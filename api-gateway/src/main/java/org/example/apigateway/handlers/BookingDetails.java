package org.example.apigateway.handlers;

import java.util.Optional;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.example.apigateway.proxies.EventInfo;
import org.example.apigateway.proxies.BookingInfo;
import org.example.apigateway.proxies.PaymentInfo;
import reactor.util.function.Tuple3;

@Getter
@Setter
@RequiredArgsConstructor
public class BookingDetails {

	private final BookingInfo bookingInfo;
	private final PaymentInfo paymentInfo;
	private final EventInfo eventInfo;

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}


	public static BookingDetails makeBookingDetails(
			Tuple3<BookingInfo, Optional<PaymentInfo>, Optional<EventInfo>> info) {
		return new BookingDetails(
				info.getT1(),
				info.getT2().orElse(new PaymentInfo()),
				info.getT3().orElse(new EventInfo())
		);
	}
}
