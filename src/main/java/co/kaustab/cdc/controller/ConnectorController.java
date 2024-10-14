package co.kaustab.cdc.controller;

import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import co.kaustab.cdc.resource.ConnectorOffsetTailInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class ConnectorController {

	@RequestMapping("/{connectorName}/tail")
	public String tail(@PathVariable("connectorName") String connectorName, Model model) {

		ArrayBlockingQueue<String> tails = ConnectorOffsetTailInfo.get(connectorName);

		log.info("tails --------> " + tails.size() + ", val=" + tails);

		model.addAttribute("data", tails);

		return "index";
	}

}
