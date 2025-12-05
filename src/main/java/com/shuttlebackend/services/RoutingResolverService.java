package com.shuttlebackend.services;

import com.shuttlebackend.entities.RouteStop;
import com.shuttlebackend.repositories.RouteStopRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutingResolverService {
    private static final Logger log = LoggerFactory.getLogger(RoutingResolverService.class);

    private final RouteStopRepository routeStopRepository;

    private static class Pair {
        final String type; // "forward", "backward", "same"
        final RouteStop p;
        final RouteStop d;
        final int delta;
        Pair(String type, RouteStop p, RouteStop d, int delta) { this.type = type; this.p = p; this.d = d; this.delta = delta; }
    }

    public static class ResolvedStop {
        public final Integer stopId;
        public final String name;
        public final Integer order;
        public final String direction;

        public ResolvedStop(Integer stopId, String name, Integer order, String direction) {
            this.stopId = stopId;
            this.name = name;
            this.order = order;
            this.direction = direction;
        }
    }

    public static class ResolutionResult {
        public final Integer routeId;
        public final String direction;
        public final ResolvedStop pickupStop;
        public final ResolvedStop dropoffStop;

        public ResolutionResult(Integer routeId, String direction, ResolvedStop pickupStop, ResolvedStop dropoffStop) {
            this.routeId = routeId;
            this.direction = direction;
            this.pickupStop = pickupStop;
            this.dropoffStop = dropoffStop;
        }
    }

    /**
     * Resolve pickup/drop stop names into route, direction, and specific route_stop entries.
     * Implements the user's five-step rules strictly and deterministically.
     */
    public Map<String, Object> resolveByNames(String pickupName, String dropoffName) {
        // STEP 1: find all route_stop entries where stop_name = pickupName and dropoffName
        List<RouteStop> pickupAll = routeStopRepository.findByStopNameIgnoreCase(pickupName.trim());
        List<RouteStop> dropAll = routeStopRepository.findByStopNameIgnoreCase(dropoffName.trim());

        if (pickupAll.isEmpty() || dropAll.isEmpty()) {
            return Map.of("error", "pickup or dropoff stop not found");
        }

        // Extract route_ids
        Set<Integer> pickupRouteIds = pickupAll.stream().map(rs -> rs.getRoute().getId()).collect(Collectors.toSet());
        Set<Integer> dropRouteIds = dropAll.stream().map(rs -> rs.getRoute().getId()).collect(Collectors.toSet());

        // Intersection
        Set<Integer> intersection = new HashSet<>(pickupRouteIds);
        intersection.retainAll(dropRouteIds);

        if (intersection.isEmpty()) {
            return Map.of("error", "pickup and dropoff are not on the same route");
        }

        // If multiple, pick the route with the shortest stop sequence
        Integer chosenRouteId;
        if (intersection.size() == 1) {
            chosenRouteId = intersection.iterator().next();
        } else {
            // pick route with shortest stop sequence
            Integer best = null;
            int bestSize = Integer.MAX_VALUE;
            for (Integer rId : intersection) {
                List<RouteStop> stops = routeStopRepository.findByRoute_IdOrderByStopOrderAsc(rId);
                if (stops.size() < bestSize) {
                    bestSize = stops.size();
                    best = rId;
                }
            }
            chosenRouteId = best;
        }

        // STEP 2: filter by route_id
        List<RouteStop> pickupCandidates = routeStopRepository.findByStopNameIgnoreCaseAndRoute_Id(pickupName.trim(), chosenRouteId);
        List<RouteStop> dropCandidates = routeStopRepository.findByStopNameIgnoreCaseAndRoute_Id(dropoffName.trim(), chosenRouteId);

        if (pickupCandidates.isEmpty() || dropCandidates.isEmpty()) {
            return Map.of("error", "pickup or dropoff not found on chosen route");
        }

        // STEP 3: determine direction using stop_order
        // Build candidate pairs with computed type and delta, but enforce stored direction matches inferred type.
        List<Pair> pairs = new ArrayList<>();
        for (RouteStop p : pickupCandidates) {
            for (RouteStop d : dropCandidates) {
                String inferredType;
                int delta;
                if (Objects.equals(p.getStopOrder(), d.getStopOrder())) {
                    inferredType = "same";
                    delta = 0;
                } else if (p.getStopOrder() < d.getStopOrder()) {
                    inferredType = "forward";
                    delta = d.getStopOrder() - p.getStopOrder();
                } else {
                    inferredType = "backward";
                    delta = p.getStopOrder() - d.getStopOrder();
                }

                // Enforce that stored direction fields correspond with inferred direction.
                // Use equalsIgnoreCase to be robust (DB uses "FORWARD"/"BACKWARD" but this tolerates "forward").
                boolean pMatchesDirection = true;
                boolean dMatchesDirection = true;
                if (!"same".equals(inferredType)) {
                    pMatchesDirection = p.getDirection() != null && p.getDirection().equalsIgnoreCase(inferredType);
                    dMatchesDirection = d.getDirection() != null && d.getDirection().equalsIgnoreCase(inferredType);
                } // for "same" we allow any stored directions (they are same stop entries)

                if ("same".equals(inferredType) || (pMatchesDirection && dMatchesDirection)) {
                    pairs.add(new Pair(inferredType, p, d, delta));
                } else {
                    // skip this pair because DB direction doesn't match inferred direction
                    // but record nothing here; we will handle fallback later if all pairs filtered out
                }
            }
        }

        // If direction-enforced filtering left us empty but there exist order-based pairs (DB inconsistent), fall back
        boolean hadOrderBasedPairs = false;
        if (pairs.isEmpty()) {
            // check if there were any order-based pairs before enforcing direction (i.e., DB inconsistent)
            for (RouteStop p : pickupCandidates) {
                for (RouteStop d : dropCandidates) {
                    if (!Objects.equals(p.getStopOrder(), d.getStopOrder())) {
                        hadOrderBasedPairs = true;
                        break;
                    }
                }
                if (hadOrderBasedPairs) break;
            }
            if (hadOrderBasedPairs) {
                // Log warning and build pairs without enforcing direction (fallback)
                log.warn("RouteStop.direction values inconsistent with stop_order inference for routeId={}, pickup='{}', dropoff='{}'. Falling back to order-based selection.",
                        chosenRouteId, pickupName, dropoffName);
                pairs.clear();
                for (RouteStop p : pickupCandidates) {
                    for (RouteStop d : dropCandidates) {
                        if (Objects.equals(p.getStopOrder(), d.getStopOrder())) {
                            pairs.add(new Pair("same", p, d, 0));
                        } else if (p.getStopOrder() < d.getStopOrder()) {
                            pairs.add(new Pair("forward", p, d, d.getStopOrder() - p.getStopOrder()));
                        } else {
                            pairs.add(new Pair("backward", p, d, p.getStopOrder() - d.getStopOrder()));
                        }
                    }
                }
            }
        }

        // Partition pairs
        List<Pair> forwardPairs = pairs.stream().filter(x -> "forward".equals(x.type)).collect(Collectors.toList());
        List<Pair> backwardPairs = pairs.stream().filter(x -> "backward".equals(x.type)).collect(Collectors.toList());
        List<Pair> samePairs = pairs.stream().filter(x -> "same".equals(x.type)).collect(Collectors.toList());

        // Prefer forward if only forward exists, or backward if only backward exists. If both exist choose smallest delta across both.
        if (!forwardPairs.isEmpty() && backwardPairs.isEmpty()) {
            Pair best = selectBestPairOrAmbiguous(forwardPairs);
            if (best == null) return ambiguousResponseWithPairs(pairs);
            return successResponse(chosenRouteId, "FORWARD", best.p, best.d);
        }

        if (!backwardPairs.isEmpty() && forwardPairs.isEmpty()) {
            Pair best = selectBestPairOrAmbiguous(backwardPairs);
            if (best == null) return ambiguousResponseWithPairs(pairs);
            return successResponse(chosenRouteId, "BACKWARD", best.p, best.d);
        }

        if (!forwardPairs.isEmpty() && !backwardPairs.isEmpty()) {
            // choose smallest delta among all non-same pairs
            List<Pair> all = new ArrayList<>(); all.addAll(forwardPairs); all.addAll(backwardPairs);
            Pair best = selectBestPairOrAmbiguous(all);
            if (best == null) return ambiguousResponseWithPairs(pairs);
            String dir = "forward".equals(best.type) ? "FORWARD" : "BACKWARD";

            // Final safety: ensure selected RouteStop entry actually matches the direction stored in DB.
            // (If we fell back earlier because directions were inconsistent this check may not hold; it's fine.)
            if (!best.p.getDirection().equalsIgnoreCase(dir) || !best.d.getDirection().equalsIgnoreCase(dir)) {
                // If mismatch, attempt to find explicit route_stop entries that match direction
                List<RouteStop> pickupInDir = pickupCandidates.stream().filter(r -> dir.equalsIgnoreCase(r.getDirection())).collect(Collectors.toList());
                List<RouteStop> dropInDir = dropCandidates.stream().filter(r -> dir.equalsIgnoreCase(r.getDirection())).collect(Collectors.toList());
                if (pickupInDir.size() == 1 && dropInDir.size() == 1) {
                    return successResponse(chosenRouteId, dir, pickupInDir.get(0), dropInDir.get(0));
                }
                // otherwise fall back to the best pair anyway
            }

            return successResponse(chosenRouteId, dir, best.p, best.d);
        }

        // only same-order pairs
        if (!samePairs.isEmpty()) {
            return Map.of("error", "Pickup and dropoff are the same stop; provide stop_id to disambiguate");
        }

        // fallback ambiguous
        return ambiguousResponseWithPairs(pairs);
    }

    // Return the best pair if a unique smallest-delta exists; otherwise non-null deterministic winner
    private Pair selectBestPairOrAmbiguous(List<Pair> list) {
        if (list.isEmpty()) return null;
        list.sort(Comparator.comparingInt(p -> p.delta));
        if (list.size() == 1) return list.get(0);
        int bestDelta = list.get(0).delta;
        // collect those with same best delta
        List<Pair> bestOnes = new ArrayList<>();
        for (Pair p : list) {
            if (p.delta == bestDelta) bestOnes.add(p); else break;
        }
        if (bestOnes.size() == 1) return bestOnes.get(0);

        // Deterministic tie-breaker: prefer pair with smallest pickup order, then smallest drop order, then lowest pickup id
        bestOnes.sort(Comparator
                .comparingInt((Pair p) -> p.p.getStopOrder())
                .thenComparingInt(p -> p.d.getStopOrder())
                .thenComparingInt(p -> p.p.getId()));
        return bestOnes.get(0);
    }

    private Map<String, Object> ambiguousResponseWithPairs(List<Pair> pairs) {
        // Log for server-side debugging
        log.warn("Routing ambiguous: candidate pairs:\n{}", pairs.stream().map(p -> String.format("pickup=%d(%s,%d) drop=%d(%s,%d) type=%s delta=%d",
                p.p.getId(), p.p.getDirection(), p.p.getStopOrder(), p.d.getId(), p.d.getDirection(), p.d.getStopOrder(), p.type, p.delta)).collect(Collectors.joining("\n")));

        List<Map<String,Object>> matches = new ArrayList<>();
        for (Pair p : pairs) {
            matches.add(Map.of(
                    "pickup_stop_id", p.p.getId(),
                    "pickup_direction", p.p.getDirection(),
                    "pickup_order", p.p.getStopOrder(),
                    "dropoff_stop_id", p.d.getId(),
                    "dropoff_direction", p.d.getDirection(),
                    "dropoff_order", p.d.getStopOrder(),
                    "type", p.type,
                    "delta", p.delta
            ));
        }

        return Map.of("error", "The stop name appears in multiple directions. Provide the stop_id to clarify.", "matches", matches);
    }

    private Map<String,Object> successResponse(Integer routeId, String dir, RouteStop p, RouteStop d) {
        Map<String,Object> pickup = Map.of(
                "stop_id", p.getId(),
                "name", p.getStopName(),
                "order", p.getStopOrder(),
                "direction", p.getDirection()
        );
        Map<String,Object> drop = Map.of(
                "stop_id", d.getId(),
                "name", d.getStopName(),
                "order", d.getStopOrder(),
                "direction", d.getDirection()
        );
        return Map.of(
                "route_id", routeId,
                "direction", dir,
                "pickupStop", pickup,
                "dropoffStop", drop
        );
    }
}
