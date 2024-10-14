package co.kaustab.cdc.message.builder;

import co.kaustab.cdc.model.BaseEventModel;
import co.kaustab.cdc.model.MessageSinkModel;

public interface MessageBuilder {

	public BaseEventModel build(MessageSinkModel messageSinkModel);

}
