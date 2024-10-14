package co.kaustab.cdc.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Component;

import co.kaustab.cdc.model.MessageSinkModel;
import co.kaustab.cdc.model.PipelineSinkConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RdbmsCliUtils {

	public int update(MessageSinkModel messageSinkModel, JdbcTemplate jdbcTemplate,
			PipelineSinkConfig sinkConfiguration) {

		String tableName = messageSinkModel.getSource().split("\\.")[1];
		Map<String, Object> newData = messageSinkModel.getNewMessages();
		Map<String, Object> oldData = messageSinkModel.getOldMessages();
		String pkId = sinkConfiguration.getPrimaryFields();

		if (!recordExists(jdbcTemplate, tableName, pkId, oldData.get(pkId))
				|| sinkConfiguration.getInsertMode().equalsIgnoreCase("insert")) {
			int resp = insert(messageSinkModel, jdbcTemplate);
			return resp;
		}

		boolean multi = false;
		StringBuffer sqlBuilder = new StringBuffer("UPDATE ").append(tableName).append(" SET ");
		for (Map.Entry<String, Object> entry : newData.entrySet()) {
			if (!multi) {
				sqlBuilder.append(entry.getKey()).append(" = ?");
				multi = true;
			} else {
				sqlBuilder.append(",").append(entry.getKey()).append(" = ?");
			}
		}
		sqlBuilder.append(" WHERE ").append(pkId).append("= ?");

		int resp = jdbcTemplate.update(sqlBuilder.toString(), new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int i = 0;
				for (Object obj : newData.values()) {
					ps.setObject(++i, obj);
				}
				ps.setObject(++i, newData.get(pkId));
			}
		});

		return resp;
	}

	public int insert(MessageSinkModel messageSinkModel, JdbcTemplate jdbcTemplate) {

		String tableName = messageSinkModel.getSource().split("\\.")[1];
		Map<String, Object> newData = messageSinkModel.getNewMessages();
		String columns = String.join(",", newData.keySet());
		Collection<Object> vals = newData.values();
		String valuePlaceholders = "";
		for (Object obj : vals) {
			valuePlaceholders += valuePlaceholders.isEmpty() ? "?" : ",?";
		}

		String sql = new StringBuffer("INSERT INTO ").append(tableName).append("(").append(columns).append(") ")
				.append("VALUES (").append(valuePlaceholders).append(")").toString();

		int resp = jdbcTemplate.update(sql, new PreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				int i = 0;
				for (Object obj : vals) {
					ps.setObject(++i, obj);
				}
			}
		});

		return resp;
	}

	public int delete(MessageSinkModel messageSinkModel, JdbcTemplate jdbcTemplate,
			PipelineSinkConfig sinkConfiguration) {

		String tableName = messageSinkModel.getSource().split("\\.")[1];
		Map<String, Object> oldData = messageSinkModel.getOldMessages();
		String pkId = sinkConfiguration.getPrimaryFields();

		String deleteQuery = "delete from " + tableName + " where id = ?";
		int resp = jdbcTemplate.update(deleteQuery, oldData.get(pkId));
		return resp;
	}

	private boolean recordExists(JdbcTemplate jdbcTemplate, String tableName, String pkId, Object pkValue) {
		String sql = "SELECT count(*) FROM " + tableName + " WHERE " + pkId + " = ?";
		Integer count = jdbcTemplate.queryForObject(sql, new Object[] { pkValue }, Integer.class);
		return count != null && count > 0;
	}
}
