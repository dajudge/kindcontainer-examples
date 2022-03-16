package com.dajudge.kindcontainer.examples.operator.transport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GreeterStatus {
}
