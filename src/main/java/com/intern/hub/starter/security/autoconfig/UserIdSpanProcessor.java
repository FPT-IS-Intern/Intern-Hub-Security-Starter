package com.intern.hub.starter.security.autoconfig;

import com.intern.hub.starter.security.context.AuthContextHolder;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class UserIdSpanProcessor implements SpanProcessor {

  private static final AttributeKey<Long> ENDUSER_ID = AttributeKey.longKey("enduser.id");

  @Override
  public void onStart(Context context, ReadWriteSpan readWriteSpan) {
    if(!AuthContextHolder.AUTH_CONTEXT.isBound()) return;
    AuthContextHolder.get()
        .filter(ctx -> ctx.authenticated() && ctx.userId() != null)
        .ifPresent(ctx -> readWriteSpan.setAttribute(ENDUSER_ID, ctx.userId()));
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan readableSpan) {
  }

  @Override
  public boolean isEndRequired() {
    return false;
  }

}
