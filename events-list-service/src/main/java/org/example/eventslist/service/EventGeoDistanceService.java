package org.example.eventslist.service;

import org.example.eventslist.model.elasticsearch.Event;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.*;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventGeoDistanceService {

    @Autowired
    private OpenSearchClient openSearchClient;

    public List<Event> findEventsSortedByDistance(double lat, double lon) throws Exception {
        GeoDistanceSort geoDistanceSort = new GeoDistanceSort.Builder()
                .field("location")
                .location(new GeoLocation.Builder().latlon(new LatLonGeoLocation.Builder().lat(lat).lon(lon).build()).build())
                .order(SortOrder.Asc)
                .unit(DistanceUnit.Kilometers)

                .mode(SortMode.Min)
                .distanceType(GeoDistanceType.Arc)
                .build();

        SortOptions sortOptions = new SortOptions.Builder()
                .geoDistance(geoDistanceSort)
                .build();

        Query geoDistanceQuery = QueryBuilders.geoDistance()
                .field("location")
                .distance("100km")
                .location(new GeoLocation.Builder().latlon(new LatLonGeoLocation.Builder().lat(lat).lon(lon).build()).build())
                .build()
                ._toQuery();

        SearchRequest searchRequest = new SearchRequest.Builder()
                .index("events")
                .query(geoDistanceQuery)
                .size(100)
                .sort(sortOptions)
                .build();

        SearchResponse<Event> response = openSearchClient.search(searchRequest, Event.class);

        return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

}