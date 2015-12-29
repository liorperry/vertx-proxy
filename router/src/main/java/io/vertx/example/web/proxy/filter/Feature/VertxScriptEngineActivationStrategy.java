package io.vertx.example.web.proxy.filter.Feature;

import org.togglz.core.activation.Parameter;
import org.togglz.core.activation.ParameterBuilder;
import org.togglz.core.logging.Log;
import org.togglz.core.logging.LogFactory;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.spi.ActivationStrategy;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.util.Strings;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VertxScriptEngineActivationStrategy implements ActivationStrategy {
    private final Log log = LogFactory.getLog(VertxScriptEngineActivationStrategy.class);

    public static final String ID = "script";
    public static final String PARAM_SCRIPT = "script";
    public static final String PARAM_LANG = "lang";

    private final ScriptEngineManager engineManager;

    public VertxScriptEngineActivationStrategy() {
        engineManager = new ScriptEngineManager();
    }

    public String getId() {
        return ID;
    }

    public String getName() {
        return "Java Scripting API";
    }

    public boolean isActive(FeatureState featureState, FeatureUser user) {

        String lang = featureState.getParameter(PARAM_LANG);
        String script = featureState.getParameter(PARAM_SCRIPT);

        ScriptEngine engine = engineManager.getEngineByName(lang);
        if (engine == null) {
            log.error("Could not find script engine for: " + lang);
            return false;
        }

        populateEngineParams(featureState, user, engine);
        try {

            Object result = engine.eval(script);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }

        } catch (ScriptException e) {
            log.error("Could not evaluate script for Feature " + featureState.getFeature().name() + ": " + e.getMessage());
        }
        return false;

    }

    /**
     * populate engine scrips parameters
     * @param featureState
     * @param user
     * @param engine
     */
    protected void populateEngineParams(FeatureState featureState, FeatureUser user, ScriptEngine engine) {
        engine.put("user", user);
        engine.put("date", new Date());
        //populate entire param map
        featureState.getParameterMap().entrySet().stream().forEach(entry -> { engine.put(entry.getKey(),entry.getValue()); });
    }

    public Parameter[] getParameters() {
        return new Parameter[] {
                new ScriptLanguageParameter(engineManager),
                ParameterBuilder.create(PARAM_SCRIPT).label("Script").largeText()
                        .description("The script to check if the Feature is active. " +
                                "The script context provides access to some default objects. " +
                                "The variable 'user' refers to the current acting FeatureUser " +
                                "and 'date' to the current time represented as a java.util.Date.")
        };
    }

    private static class ScriptLanguageParameter implements Parameter {

        private List<String> languages = new ArrayList<String>();

        public ScriptLanguageParameter(ScriptEngineManager engineManager) {
            for (ScriptEngineFactory factory : engineManager.getEngineFactories()) {
                languages.add(factory.getLanguageName());
            }
        }

        @Override
        public String getName() {
            return PARAM_LANG;
        }

        @Override
        public String getLabel() {
            return "Language";
        }

        @Override
        public String getDescription() {
            return "The script language to use. Your system seems to support the following languages: " +
                    Strings.join(languages, ", ");
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public boolean isLargeText() {
            return false;
        }

        @Override
        public boolean isValid(String value) {
            return Strings.isNotBlank(value) && languages.contains(value);
        }

    }
}
