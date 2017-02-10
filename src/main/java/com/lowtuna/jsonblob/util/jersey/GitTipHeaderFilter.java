package com.lowtuna.jsonblob.util.jersey;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class GitTipHeaderFilter implements ContainerResponseFilter {

  @Override
  public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
    response.getHttpHeaders().add("X-Hello-Human", "If you feel JSON Blob is useful, please consider supporting it! https://www.gittip.com/tburch/");
    return response;
  }

}
