package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling;

import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.ForbiddenException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.HttpClientException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.ImATeapotException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.ResourceNotFoundException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.UnauthorizedException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client.UnavailableForLegalReasonsException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server.BadGatewayException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server.GatewayTimeOutException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server.HttpServerException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server.HttpVersionNotSupportedException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server.NotImplementedException;
import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class EndpointErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    final int status = response.status();
    if (400 <= status && status < 500) {
      return clientException(methodKey, response);
    } else if (500 <= status && status < 600) {
      return serverException(methodKey, response);
    } else {
      return genericHttpException(methodKey, response);
    }
  }

  private HttpClientException clientException(String methodKey, Response response) {
    final int status = response.status();
    switch (status) {
      case 401:
        return new UnauthorizedException(methodKey, response);
      case 403:
        return new ForbiddenException(methodKey, response);
      case 404:
        return new ResourceNotFoundException(methodKey, response);
      case 413:
        return new ImATeapotException(methodKey, response);
      case 451:
        return new UnavailableForLegalReasonsException(methodKey, response);
      default:
        return new HttpClientException(methodKey, response);
    }
  }

  private Exception genericHttpException(String methodKey, Response response) {
    return new HttpException(methodKey, response);
  }

  private HttpServerException serverException(String methodKey, Response response) {
    final int status = response.status();
    switch (status) {
      case 501:
        return new NotImplementedException(methodKey, response);
      case 502:
        return new BadGatewayException(methodKey, response);
      case 503:
        return new ServiceUnavailableException(methodKey, response);
      case 504:
        return new GatewayTimeOutException(methodKey, response);
      case 505:
        return new HttpVersionNotSupportedException(methodKey, response);
      default:
        return new HttpServerException(methodKey, response);
    }
  }
}
