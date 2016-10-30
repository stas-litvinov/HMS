package ru.innopolis.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.innopolis.constants.AuthorizationConstant;
import ru.innopolis.dao.IClientDAOService;
import ru.innopolis.dao.entity.Client;
import ru.innopolis.dao.imp.ClientDAOService;
import ru.innopolis.helpers.PasswordHelper;
import ru.innopolis.models.AuthorizationRequestModel;
import ru.innopolis.models.AuthorizationResponseModel;
import ru.innopolis.models.ErrorResponse;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Создано: Денис
 * Дата:  29.10.2016
 * Описание: Котроллер аутентификации
 */

@RestController
public class AuthorizationController extends BaseRestController {

    private static final String CLIENT_DOES_NOT_EXIT_MESSAGE = "client.does.not.exit";
    private Logger logger = Logger.getLogger(AuthorizationController.class.getName());

    @Autowired
    public AuthorizationController(MessageSource messageSource) {
        super(messageSource);
    }

    @PostMapping("/auth/login")
    public ResponseEntity authenticate(@Valid @RequestBody AuthorizationRequestModel model, Errors bindingResult, HttpSession session) {
        ResponseEntity response = getValidationErrorResponse(bindingResult);
        if (response == null) {
            IClientDAOService service = new ClientDAOService();
            try {
                String password = model.getPassword();
                String encryptPassword = PasswordHelper.encrypt(password);

                Client client = service.findClient(model.getMail(), encryptPassword);
                if (client != null) {
                    session.setAttribute(AuthorizationConstant.AUTHORIZATION_KEY, Boolean.TRUE);
                    AuthorizationResponseModel responseModel = buildResponseModel(client);
                    response = new ResponseEntity<>(responseModel, HttpStatus.OK);
                } else {
                    String message = getMessage(CLIENT_DOES_NOT_EXIT_MESSAGE, null);
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.addCommonError(message);
                    response = new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                response = getGeneralErrorResponse();
            }
        }
        return response;
    }

    private AuthorizationResponseModel buildResponseModel(Client client) {
        AuthorizationResponseModel responseModel = new AuthorizationResponseModel();
        responseModel.setId(client.getId());
        responseModel.setFirstName(client.getFirstName());
        responseModel.setSecondName(client.getSecondName());
        responseModel.setFatherName(client.getFatherName());
        return responseModel;
    }
}