package co.kaustab.cdc.message.builder;

import java.util.Calendar;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.kaustab.cdc.model.BaseEventModel;
import co.kaustab.cdc.model.MessageEventResponseModel;
import co.kaustab.cdc.model.MessageSinkModel;

@Component("CdcRawDataKafkaMessageBuilder")
public class CdcRawDataKafkaMessageBuilder implements MessageBuilder {

	@Override
	public BaseEventModel build(final MessageSinkModel messageSinkModel) {

		return new MessageEventResponseModel() {
			{
				this.setEventName("CDC_"+messageSinkModel.getOperation().toString());
				this.setEventType(messageSinkModel.getSource());
				this.setCreatedTime(Calendar.getInstance());
			}
			
			@JsonProperty("cdc_raw_data")
			public String getRawData() {
				return messageSinkModel.getSourceRecord().value().toString();
			}
			
			@JsonProperty("source_offset")
			public Map<String, ?> getSourceOffset() {
				return messageSinkModel.getSourceRecord().sourceOffset();
			}
			
		};
	}
	
}
