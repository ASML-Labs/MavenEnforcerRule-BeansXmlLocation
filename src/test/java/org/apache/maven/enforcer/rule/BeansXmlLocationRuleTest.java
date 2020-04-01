package org.apache.maven.enforcer.rule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BeansXmlLocationRuleTest {

	@InjectMocks
	BeansXmlLocationRule beansXmlLocationRule;

	@Mock
	private MavenProject mavenProject;
	
	@Mock
	private Log log;

	@Mock
	private EnforcerRuleHelper helper;


	@BeforeEach 
	public void beforeEach() throws Exception {
		Mockito.lenient().when(helper.getLog()).thenReturn(mock(Log.class));
	}

	@Test
	public void getCacheIdTest() {
		assertNotNull(beansXmlLocationRule);
		assertEquals("", beansXmlLocationRule.getCacheId());
	}

	@Test
	public void isCachebleTest() {
		assertNotNull(beansXmlLocationRule);
		assertFalse(beansXmlLocationRule.isCacheable());
	}

	@Test
	public void isResultValidAlwaysFalseTest() {
		assertNotNull(beansXmlLocationRule);
		assertFalse(beansXmlLocationRule.isResultValid(null));
		assertFalse(beansXmlLocationRule.isResultValid(new BeansXmlLocationRule()));
	}

	@Test
	public void executeNullPointerException() throws EnforcerRuleException {
		assertNotNull(beansXmlLocationRule);
		assertThrows(NullPointerException.class, () -> {
			beansXmlLocationRule.execute(null);
		});
	}

	@Test
	public void executeEnforcerRuleExceptionWar() throws EnforcerRuleException, ExpressionEvaluationException {
		Mockito.lenient().when(helper.evaluate("${project}")).thenReturn(mavenProject);
		Mockito.lenient().when(mavenProject.getPackaging()).thenReturn("war");
		Mockito.lenient().when(mavenProject.getBasedir()).thenReturn(new File("src/test/resources"));
		//given  
		assertNotNull(beansXmlLocationRule);
		String expectedMessage = "war packaging demands that beans.xml exists here: src\\test\\resources\\src\\main\\webapp\\WEB-INF\\beans.xml";
	
		//when
		Exception exception = assertThrows(EnforcerRuleException.class, () -> {
			beansXmlLocationRule.execute(helper);
		});
		
		//then		
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage),
				"actualMessage: '" + actualMessage + "' expectedMessage : '" + expectedMessage + "'");
	}
	
	@Test
	public void executeEnforcerRuleExceptionWarIsNotEjbOrJar() throws EnforcerRuleException, ExpressionEvaluationException, IOException {
		File resourcesFolder = new File("src/test/resources");
		Path notWarLocation = resourcesFolder.toPath().resolve(BeansXmlLocationRule.EJB_OR_JAR);
		Files.createDirectories(notWarLocation.getParent().toAbsolutePath());
		Files.write(notWarLocation.toAbsolutePath(), "empty".getBytes(), StandardOpenOption.CREATE);
		
		Mockito.lenient().when(helper.evaluate("${project}")).thenReturn(mavenProject);
		Mockito.lenient().when(mavenProject.getPackaging()).thenReturn("war");
		Mockito.lenient().when(mavenProject.getBasedir()).thenReturn(new File("src/test/resources"));

		//given  
		assertNotNull(beansXmlLocationRule);
		String expectedMessage = "File: src\\test\\resources\\src\\main\\resources\\META-INF\\beans.xml must be moved to: src\\test\\resources\\src\\main\\webapp\\WEB-INF\\beans.xml";

		//when
		Exception exception = assertThrows(EnforcerRuleException.class, () -> {
			beansXmlLocationRule.execute(helper);
		});
		
		//then		
		String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(expectedMessage),
				"actualMessage: '" + actualMessage + "' expectedMessage : '" + expectedMessage + "'");
		Files.deleteIfExists(notWarLocation.toAbsolutePath());
		//no exception == success :)
	}

	@Test
	public void executeEnforcerRuleCorrect() throws EnforcerRuleException, ExpressionEvaluationException, IOException {
		File resourcesFolder = new File("src/test/resources");
		Path warLocation = resourcesFolder.toPath().resolve(BeansXmlLocationRule.WEBAPP);
		Files.createDirectories(warLocation.getParent().toAbsolutePath());
		Files.write(warLocation.toAbsolutePath(), "empty".getBytes(), StandardOpenOption.CREATE);
		
		Mockito.lenient().when(helper.evaluate("${project}")).thenReturn(mavenProject);
		Mockito.lenient().when(mavenProject.getPackaging()).thenReturn("war");
		Mockito.lenient().when(mavenProject.getBasedir()).thenReturn(new File("src/test/resources"));

		//given  
		assertNotNull(beansXmlLocationRule);
		String expectedMessage = "File: src\\test\\resources\\src\\main\\resources\\META-INF\\beans.xml must be moved to src\\test\\resources\\src\\main\\webapp\\WEB-INF\\beans.xml";

		//when
		beansXmlLocationRule.execute(helper);
		
		Files.deleteIfExists(warLocation.toAbsolutePath());
		//no exception == success :)
	}
	
}