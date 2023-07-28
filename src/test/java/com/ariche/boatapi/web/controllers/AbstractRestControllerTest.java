package com.ariche.boatapi.web.controllers;

import com.ariche.boatapi.MyBoatApplication;
import com.ariche.boatapi.web.errors.ExceptionTranslator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MyBoatApplication.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
public abstract class AbstractRestControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private ExceptionTranslator exceptionHandler;

    @Autowired
    private Filter springSecurityFilterChain;

    protected MockMvc restMock;
    protected String END_POINT;

    MockMvc buildRestMock(Object resource) {
        return MockMvcBuilders
            .standaloneSetup(resource)
            .setControllerAdvice(exceptionHandler)
            .apply(springSecurity(springSecurityFilterChain))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setConversionService(createFormattingConversionService())
            .build();
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.END_POINT = getEndpoint();
        this.restMock = buildRestMock(buildResource());
    }

    public abstract Object buildResource();

    public abstract String getEndpoint();

    protected Map<String, RequestBuilder> getDynamicTests() {
        return Collections.emptyMap();
    }


    /**
     * Create a FormattingConversionService which use ISO date format, instead of the localized one.
     *
     * @return the FormattingConversionService
     */
    public static FormattingConversionService createFormattingConversionService() {
        DefaultFormattingConversionService dfcs = new DefaultFormattingConversionService();
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(dfcs);
        return dfcs;
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

  /*  @TestFactory
    Stream<DynamicTest> test_unauthorizedRoutes() {
        return getDynamicTests().entrySet()
            .stream()
            .map(entry -> DynamicTest.dynamicTest(
                entry.getKey() + "_Unauthorized",
                () -> restMock.perform(entry.getValue()).andExpect(status().isForbidden())));
    }*/

    protected static <T> Page<T> readMvcResultAsPage(MvcResult result, Class<T> tClass, ObjectMapper mapper) throws IOException {
        final JSONObject out = new JSONObject(result.getResponse().getContentAsString());
        final String jArray = out.getJSONArray("content").toString();
        long total = out.optLong("numberOfElements", 0L);
        final List<T> list = mapper.readValue(jArray, mapper.getTypeFactory().constructCollectionType(List.class, tClass));
        return new PageImpl<>(list, Pageable.unpaged(), total);
    }

    public <T> Page<T> readMvcResultAsPage(MvcResult result, Class<T> tClass) throws IOException {
        return readMvcResultAsPage(result, tClass, objectMapper);
    }

    public <T> T readMvcResultAs(MvcResult result, Class<T> tClass) throws IOException {
        return objectMapper.readValue(result.getResponse().getContentAsString(), tClass);
    }

}
