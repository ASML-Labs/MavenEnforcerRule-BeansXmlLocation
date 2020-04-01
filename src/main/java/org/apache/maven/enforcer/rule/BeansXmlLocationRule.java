package org.apache.maven.enforcer.rule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

public class BeansXmlLocationRule implements EnforcerRule {

	protected static Path WEBAPP = Paths.get("src/main/webapp/WEB-INF/beans.xml");
	protected static Path EJB_OR_JAR = Paths.get("src/main/resources/META-INF/beans.xml");
	
	public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {

		try {
			MavenProject project = (MavenProject) helper.evaluate("${project}");

			if (project.getPackaging().contentEquals("war")) {
				// from https://docs.oracle.com/javaee/6/tutorial/doc/gjbnz.html
				// For a web application, the beans.xml file must be in the WEB-INF directory.
				Path currentModuleBaseDir = project.getBasedir().toPath();

				final Path invalidBeansXml = currentModuleBaseDir.resolve(EJB_OR_JAR);
				final Path validBeansXml = currentModuleBaseDir.resolve(WEBAPP);
				if (Files.exists(invalidBeansXml)) {
					throw new EnforcerRuleException("File: " + invalidBeansXml + " must be moved to: "
							+ Paths.get(currentModuleBaseDir.toString(), WEBAPP.toString()));
				}
				if (!Files.exists(validBeansXml)) {
					throw new EnforcerRuleException("war packaging demands that beans.xml exists here: "
							+ Paths.get(currentModuleBaseDir.toString(), WEBAPP.toString()));
				}
			}
		} catch (ExpressionEvaluationException e) {
			throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
		}
	}

	public String getCacheId() {
		return "";
	}

	public boolean isCacheable() {
		return false;
	}

	public boolean isResultValid(EnforcerRule arg0) {
		return false;
	}
}
