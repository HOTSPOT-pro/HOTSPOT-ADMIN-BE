package hotspot.admin.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import hotspot.admin.family.domain.ApplyType;
import hotspot.admin.family.domain.FamilyApplyStatus;

@Configuration
public class PathVariableEnumConverterConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new CaseInsensitiveApplyTypeConverter());
        registry.addConverter(new CaseInsensitiveFamilyApplyStatusConverter());
    }

    private static class CaseInsensitiveApplyTypeConverter implements Converter<String, ApplyType> {
        @Override
        public ApplyType convert(String source) {
            if (source == null) {
                return null;
            }

            for (ApplyType type : ApplyType.values()) {
                if (type.name().equalsIgnoreCase(source)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid ApplyType: " + source);
        }
    }

    private static class CaseInsensitiveFamilyApplyStatusConverter implements Converter<String, FamilyApplyStatus> {
        @Override
        public FamilyApplyStatus convert(String source) {
            if (source == null) {
                return null;
            }

            for (FamilyApplyStatus status : FamilyApplyStatus.values()) {
                if (status.name().equalsIgnoreCase(source)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid FamilyApplyStatus: " + source);
        }
    }
}
