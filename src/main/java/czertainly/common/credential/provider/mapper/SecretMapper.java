package czertainly.common.credential.provider.mapper;

import com.czertainly.api.model.connector.secrets.CreateSecretRequestDto;
import com.czertainly.api.model.connector.secrets.SecretContentResponseDto;
import com.czertainly.api.model.connector.secrets.SecretResponseDto;
import czertainly.common.credential.provider.dao.entity.Secret;
import czertainly.common.credential.provider.dao.entity.SecretCompositeId;

public final class SecretMapper {

    private SecretMapper() {
        // Utility class.
    }

    public static Secret toEntity(CreateSecretRequestDto request) {
        return Secret.builder()
                .id(new SecretCompositeId(request.getName(), request.getSecret().getType(), "1"))
                .secretContent(request.getSecret())
                .secretAttributes(request.getSecretAttributes())
                .vaultAttributes(request.getVaultAttributes())
                .build();
    }

    public static SecretResponseDto toResponse(Secret entity) {
        return SecretResponseDto.builder()
                .name(entity.getId().getName())
                .type(entity.getId().getSecretType())
                .version(entity.getId().getVersion())
                .build();
    }

    public static SecretContentResponseDto toContentResponse(Secret entity) {
        return SecretContentResponseDto.builder()
                .version(entity.getId().getVersion())
                .content(entity.getSecretContent())
                .build();
    }
}
