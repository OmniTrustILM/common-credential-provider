package czertainly.common.credential.provider.api;

import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.interfaces.connector.AttributesController;
import com.czertainly.api.model.client.attribute.RequestAttribute;
import com.czertainly.api.model.common.attribute.common.BaseAttribute;
import czertainly.common.credential.provider.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/credentialProvider/{kind}/attributes")
public class AttributesControllerImpl implements AttributesController {

    @Autowired
    public void setAttributeService(AttributeService attributeService) {
        this.attributeService=attributeService;
    }

    private AttributeService attributeService;

    @Override
    public List<BaseAttribute> listAttributeDefinitions(@PathVariable String kind) {
        return attributeService.getAttributes(kind);
    }

    @Override
    public void validateAttributes(@PathVariable String kind, @RequestBody List<RequestAttribute> attributes) throws ValidationException {
        attributeService.validateAttributes(kind, attributes);
    }
}
