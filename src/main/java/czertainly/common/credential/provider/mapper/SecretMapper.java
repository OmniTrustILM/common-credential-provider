package czertainly.common.credential.provider.mapper;

import com.czertainly.api.model.connector.secrets.CreateSecretRequestDto;
import com.czertainly.api.model.connector.secrets.SecretContentResponseDto;
import com.czertainly.api.model.connector.secrets.SecretResponseDto;
import czertainly.common.credential.provider.dao.entity.Secret;

public final class SecretMapper {

    private SecretMapper() {
        // Utility class.
    }

    public static Secret toEntity(CreateSecretRequestDto request) {
        return Secret.builder()
                .name(request.getName())
                .secretVersion("1")
                .secretType(request.getSecret().getType())
                .secretContent(request.getSecret())
                .secretAttributes(request.getSecretAttributes())
                .vaultAttributes(request.getVaultAttributes())
                .build();
    }

    public static SecretResponseDto toResponse(Secret entity) {
        return SecretResponseDto.builder()
                .name(entity.getName())
                .type(entity.getSecretType())
                .version(entity.getSecretVersion())
                .build();
    }

    public static SecretContentResponseDto toContentResponse(Secret entity) {
        return SecretContentResponseDto.builder()
                .version(entity.getSecretVersion())
                .content(entity.getSecretContent())
                .build();
    }
}
