package czertainly.common.credential.provider.api;

import com.czertainly.api.exception.AlreadyExistException;
import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.interfaces.connector.secrets.SecretController;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import com.czertainly.api.model.connector.secrets.CreateSecretRequestDto;
import com.czertainly.api.model.connector.secrets.SecretContentResponseDto;
import com.czertainly.api.model.connector.secrets.SecretRequestDto;
import com.czertainly.api.model.connector.secrets.SecretResponseDto;
import com.czertainly.api.model.connector.secrets.SecretType;
import com.czertainly.api.model.connector.secrets.UpdateSecretRequestDto;
import czertainly.common.credential.provider.service.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/secretProvider/secrets")
public class SecretControllerImpl implements SecretController {
    private final SecretService secretService;

    @Autowired
    public SecretControllerImpl(SecretService secretService) {
        this.secretService = secretService;
    }

    @Override
    public SecretContentResponseDto getSecretContent(SecretRequestDto request, String version) throws NotFoundException {
        return secretService.getSecretContent(request, version);
    }

    @Override
    public SecretResponseDto createSecret(CreateSecretRequestDto request) throws AlreadyExistException {
        return secretService.createSecret(request);
    }

    @Override
    public SecretResponseDto updateSecret(UpdateSecretRequestDto request) throws NotFoundException {
        return secretService.updateSecret(request);
    }

    @Override
    public void deleteSecret(SecretRequestDto request) throws NotFoundException {
        secretService.deleteSecret(request);
    }

    @Override
    public SecretResponseDto rotateSecret(SecretRequestDto request) throws NotFoundException {
        return secretService.rotateSecret(request);
    }

    @Override
    public List<BaseAttribute> getRotateAttributes() throws NotFoundException {
        return secretService.getRotateAttributes();
    }

    @Override
    public List<BaseAttribute> getSecretAttributes(SecretType secretType) {
        return secretService.getSecretAttributes(secretType);
    }
}
