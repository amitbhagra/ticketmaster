package org.example.apigateway.proxies;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventInfo {
    private Long id;
    private String name;
    private String description;
    private String artist;
    private String venue;
    private String date;
    private String time;
}
