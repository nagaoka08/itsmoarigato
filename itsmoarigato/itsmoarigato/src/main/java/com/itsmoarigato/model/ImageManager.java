package com.itsmoarigato.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.stereotype.Component;

import com.itsmoarigato.Image;

@Component
public class ImageManager {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public Integer add(final InputStream inputStream, final String fromUser) {
		LobHandler lobHandler= new DefaultLobHandler();
		jdbcTemplate.execute("insert into image_tbl (from_user,created,contents) values (?,?,?)",new AbstractLobCreatingPreparedStatementCallback(lobHandler) {
			@Override
			protected void setValues(PreparedStatement ps, LobCreator lobCreator)
					throws SQLException, DataAccessException {
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[32768]; // この値は適当に変更してください
				int size = 0;
				    
				try {
					while((size = inputStream.read(buf, 0, buf.length)) != -1) {
						out.write(buf, 0, size);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}           
				
				ps.setString(1, fromUser);
				ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
		        lobCreator.setBlobAsBytes(ps, 3, out.toByteArray());
			}
		}
		);

		Integer imageId = jdbcTemplate.queryForObject("select max(id) from image_tbl where from_user = ?",
				Integer.class,
				fromUser);
		
		
		return imageId;
	}

	public Image findImageById(int imageId) {
		return jdbcTemplate.queryForObject("select id,contents from image_tbl where id = ?", new RowMapper<Image>(){
			@Override
			public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
				int id = rs.getInt("id");
				DefaultLobHandler lobHandler = new DefaultLobHandler();
				byte[] contents = lobHandler.getBlobAsBytes(rs, "contents");
				return new Image(id, contents);
			}
		},imageId);
	}
}
