package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.event.OnFeedEvent;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;

@Component
@Qualifier(DBStorageConsts.QUALIFIER)
public class DBFeedStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public DBFeedStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<FeedEvent> getFeed(int userId) {
        String sqlFeeds = "select EVENTID, EVENTTIMESTAMP, USERID, " +
                "E.NAME TYPENAME, O.NAME OPNAME, ENTITYID from EVENTS " +
                "INNER JOIN EVENTTYPES E on EVENTS.EVENTTYPE = E.TYPEID " +
                "INNER JOIN OPERATIONS O on O.OPERATIONID = EVENTS.OPERATION " +
                "where USERID = ?";

        return jdbcTemplate.query(sqlFeeds, (rs, rowNum) -> makeFeed(rs), userId);
    }

    private FeedEvent makeFeed(ResultSet rs) throws SQLException {
        FeedEvent feed = new FeedEvent(
                rs.getInt("EventID"),
                rs.getTimestamp("EventTimestamp").getTime(),
                rs.getInt("UserID"),
                EventType.valueOf(rs.getString("TypeName")),
                Operation.valueOf(rs.getString("OpName")),
                rs.getInt("EntityID")
        );
        return feed;
    }

    @EventListener
    public void handleOnFeedEvent(OnFeedEvent event) {
        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
        int userId = event.getUserId();
        int type = event.getFeedDetails().getType().getId();
        int operation = event.getFeedDetails().getOperation().getId();
        int entityId = event.getEntityId();

        String sqlFeedEvent =
                "insert into EVENTS (EVENTTIMESTAMP, USERID, EVENTTYPE, OPERATION, ENTITYID) " +
                        "values ( ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlFeedEvent, timestamp, userId, type, operation, entityId);
    }
}
