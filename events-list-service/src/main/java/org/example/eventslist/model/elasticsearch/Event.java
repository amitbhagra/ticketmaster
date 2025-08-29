package org.example.eventslist.model.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "events")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Text)
    private String artist;

    @Field(type = FieldType.Text)
    private String venue;

    @Field(type = FieldType.Date)
    private LocalDateTime eventDateTime;

    @Field(type = FieldType.Double)
    private BigDecimal ticketPrice;

    @Field(type = FieldType.Integer)
    private Integer totalSeats;

    @Field(type = FieldType.Object)
    private GeoPoint location;
}

