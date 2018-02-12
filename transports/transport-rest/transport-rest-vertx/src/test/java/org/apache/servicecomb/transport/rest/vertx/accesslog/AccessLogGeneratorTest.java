package org.apache.servicecomb.transport.rest.vertx.accesslog;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.servicecomb.transport.rest.vertx.accesslog.element.AccessLogItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.DatetimeConfigurableItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.HttpMethodItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.element.impl.PlainTextItem;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogItemLocation;
import org.apache.servicecomb.transport.rest.vertx.accesslog.parser.AccessLogPatternParser;
import org.apache.servicecomb.transport.rest.vertx.accesslog.placeholder.AccessLogItemTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import mockit.Deencapsulation;

public class AccessLogGeneratorTest {

  private static final AccessLogItem methodElement = new HttpMethodItem();

  private static final AccessLogItem datetimeElement = new DatetimeConfigurableItem();

  private static final AccessLogItem plainTextElement = new PlainTextItem(" - ");

  private static final AccessLogGenerator ACCESS_LOG_GENERATOR = new AccessLogGenerator("%m - %t",
      new AccessLogPatternParser() {
        @Override
        public List<AccessLogItemLocation> parsePattern(String rawPattern) {
          assertEquals("%m - %t", rawPattern);
          return Arrays.asList(
              new AccessLogItemLocation().setStart(0).setEnd(2).setPlaceHolder(AccessLogItemTypeEnum.HTTP_METHOD),
              new AccessLogItemLocation().setStart(2).setEnd(5).setPlaceHolder(AccessLogItemTypeEnum.TEXT_PLAIN),
              new AccessLogItemLocation().setStart(5).setEnd(7)
                  .setPlaceHolder(AccessLogItemTypeEnum.DATETIME_DEFAULT));
        }
      });

  @Test
  public void testConstructor() {
    AccessLogItem[] elements = Deencapsulation.getField(ACCESS_LOG_GENERATOR, "accessLogElements");
    assertEquals(3, elements.length);
    assertEquals(HttpMethodItem.class, elements[0].getClass());
    assertEquals(PlainTextItem.class, elements[1].getClass());
    assertEquals(DatetimeConfigurableItem.class, elements[2].getClass());
  }

  @Test
  public void testLog() {
    RoutingContext context = Mockito.mock(RoutingContext.class);
    HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    long startMillisecond = 1416863450581L;
    AccessLogParam accessLogParam = new AccessLogParam().setStartMillisecond(startMillisecond)
        .setRoutingContext(context);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DatetimeConfigurableItem.DEFAULT_DATETIME_PATTERN,
        DatetimeConfigurableItem.DEFAULT_LOCALE);
    simpleDateFormat.setTimeZone(TimeZone.getDefault());

    Mockito.when(context.request()).thenReturn(request);
    Mockito.when(request.method()).thenReturn(HttpMethod.DELETE);

    String log = ACCESS_LOG_GENERATOR.generateLog(accessLogParam);

    Assert.assertEquals("DELETE" + " - " + simpleDateFormat.format(startMillisecond), log);
  }
}