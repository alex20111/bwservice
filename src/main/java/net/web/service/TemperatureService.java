package net.web.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import home.common.data.Temperature;
import home.common.data.Temperature.TempRecName;
import net.web.common.Constants;
import net.web.db.entity.TempEnt;
import net.web.db.entity.User;
import net.web.db.sql.TempSql;
import net.web.enums.AccessLevel;
import net.web.enums.Website;
import net.web.service.filter.TokenValidation;
import net.web.service.model.Message;
import net.web.service.model.TempChartNumbers;
import net.web.service.model.TempObj;

@Path("temperature")
public class TemperatureService {


	private static final Logger logger = LogManager.getLogger(TemperatureService.class);
	private static  Temperature tempStorage = new Temperature();
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Context
	private transient HttpServletRequest servletRequest;

	@Path("update")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@TokenValidation
	public Response updateTemperature(Temperature temp){

		logger.debug("Updating temp: " + temp);

		Message msg = null;
		Status status = Status.FORBIDDEN;

		User userReq = (User)servletRequest.getAttribute(Constants.USER_TOKEN);// contains id and username

		//check if the user has access to edit the users..
		if (userReq.hasPermission(AccessLevel.REGULAR, userReq.getCurrentWebsite()) && Website.HEADLESS == userReq.getCurrentWebsite()) {

			TempSql sql = new TempSql();

			try {
				sql.add(convertTemp(temp));

				msg = new Message("Success", "temperature updated");
				return Response.ok().entity(msg).build();
			} catch (ClassNotFoundException | SQLException e) {
				logger.error("Error: " , e);
				msg = new Message("ERROR DB", "Error from the database");
			}

		}else {
			msg = new Message("No Access", "User does not have permission");
		}

		return Response.status(status).entity(msg).build();
	}
	@Path("temp")
	@GET
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getTemp() {

		logger.debug("Getting temperature: " + tempStorage);

		TempSql sql = new TempSql();

		
		try {
			TempEnt ent= sql.getCurrentTemp();
			convertToTempStorage(ent);


		} catch (ClassNotFoundException | SQLException e) {
			logger.error("error: " , e);
		}


		return Response.ok(tempStorage).build();
	}


	@Path("tempChart")
	@GET
	@Produces(MediaType.APPLICATION_JSON)	
	public Response getTempForChart() {

		logger.debug("getTempForChart");

		TempSql sql = new TempSql();

		LocalDateTime now = LocalDateTime.now();

		LocalDateTime start = now.minusHours(now.getHour()).minusMinutes(now.getMinute());

		logger.debug("Start - end: " + start + "  " + now);
		TempObj o = new TempObj();


		try {
			List<TempEnt>	list = sql.getDateRageTemperature(start, now);
			list.sort(Comparator.comparing(TempEnt::getLastUpdated));

			//remove duplicate dates
			Map<LocalDateTime,String > temp1 = new LinkedHashMap<>(); // date , temp
			Map<LocalDateTime,String > temp2 = new LinkedHashMap<>(); // date , temp		
			Map<LocalDateTime,String > pool = new LinkedHashMap<>(); // date , temp		

			for(TempEnt en: list) {	
				temp1.put(en.getTmp1UpdDt(), en.getTemp1());
				temp2.put(en.getTmp2UpdDt(), en.getTemp2());
				pool.put(en.getTmpPoolUpdDt(), en.getTempPool());
			}

			List<TempChartNumbers> dto = new ArrayList<>();

			for(LocalDateTime date = start; date.isBefore(now); date = date.plusMinutes(10)) {
				TempChartNumbers ch = new TempChartNumbers();
				//temp1
				for(Map.Entry<LocalDateTime, String> m1: temp1.entrySet()) {
					if (m1.getKey().isBefore(date) && m1.getKey().plusMinutes(10).isAfter(date)) {
						ch.setTemp1(m1.getValue());
						ch.setTemp1Date(m1.getKey().format(formatter));
					}
				}
				//temp2
				for(Map.Entry<LocalDateTime, String> m2: temp2.entrySet()) {
					if (m2.getKey().isBefore(date) && m2.getKey().plusMinutes(10).isAfter(date)) {
						ch.setTemp2(m2.getValue());
						ch.setTemp2Date(m2.getKey().format(formatter));

					}
				}
				//pool
				for(Map.Entry<LocalDateTime, String> p: pool.entrySet()) {
					if (p.getKey().isBefore(date) && p.getKey().plusMinutes(10).isAfter(date)) {
						ch.setPool(p.getValue());
						ch.setPoolDate(p.getKey().format(formatter));

					}
				}
				
				ch.setRecordedDateTime(date.format(formatter));
				dto.add(ch);
			}

			
			//fetch current temperature.
			TempEnt ent= sql.getCurrentTemp();
			if (ent != null) {
				o.setCurrTemp1(ent.getTemp1());
				o.setCurrTemp1Date(ent.getTmp1UpdDt().format(formatter));
				o.setCurrTemp2(ent.getTemp2());
				o.setCurrTemp2Date(ent.getTmp2UpdDt().format(formatter));
				
				
				if (ent.getTmpPoolUpdDt().isAfter(now.minusHours(now.getHour()))) {
					o.setCurrPool(ent.getTempPool());
					o.setCurrPoolDate(ent.getTmpPoolUpdDt().format(formatter));
				}
								
			}
			
			o.setTempChart(dto);
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("error: " , e);
		}


		return Response.ok(o).build();
	}

	private TempEnt convertTemp(Temperature t) {

		TempEnt e = new TempEnt();

		e.setTemp1(t.getTempSun());
		e.setTmp1UpdDt(t.getTmpSunUpdDt() != null && t.getTmpSunUpdDt().trim().length() > 0 ? LocalDateTime.parse(t.getTmpSunUpdDt(), formatter) : null);
		e.setTemp2(t.getTempShade());
		e.setTmp2UpdDt(t.getTmpShadeUpdDt() != null && t.getTmpShadeUpdDt().trim().length() > 0 ? LocalDateTime.parse(t.getTmpShadeUpdDt(), formatter) : null);
		e.setTempPool(t.getTempPool());
		e.setTmpPoolUpdDt(t.getTmpPoolUpdDt() != null && t.getTmpPoolUpdDt().trim().length() > 0 ? LocalDateTime.parse(t.getTmpPoolUpdDt(), formatter) : null);
		e.setTmpGarage(t.getTempMap().get(TempRecName.GARAGE) != null ? t.getTempMap().get(TempRecName.GARAGE) : "-99");
		e.setTmpGarageDt(t.getTempDateMap().get(TempRecName.GARAGE) != null ? LocalDateTime.parse(t.getTempDateMap().get(TempRecName.GARAGE) , formatter) : null);
		e.setLastUpdated(LocalDateTime.now());

		return e;
	}
	//
	private void convertToTempStorage(TempEnt t) {

		tempStorage.setTempSun(t.getTemp1());
		tempStorage.setTmpSunUpdDt(t.getTmp1UpdDt().format(formatter));
		tempStorage.setTempShade(t.getTemp2());
		tempStorage.setTmpShadeUpdDt(t.getTmp2UpdDt().format(formatter));
		tempStorage.setTempPool(t.getTempPool());
		tempStorage.setTmpPoolUpdDt(t.getTmpPoolUpdDt().format(formatter));

		Map<TempRecName, String> g = new HashMap<>();
		g.put(TempRecName.GARAGE, t.getTmpGarage());
		tempStorage.setTempMap(g);

		Map<TempRecName, String> gDt = new HashMap<>();
		gDt.put(TempRecName.GARAGE, t.getTmpGarageDt().format(formatter));
		tempStorage.setTempDateMap(gDt);



	}

}
