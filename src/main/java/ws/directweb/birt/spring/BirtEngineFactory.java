package ws.directweb.birt.spring;

import java.util.logging.Level;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.core.runtime.Assert;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class BirtEngineFactory implements FactoryBean<IReportEngine>, ApplicationContextAware, DisposableBean {
	
	private ApplicationContext context;
	private IReportEngine iReportEngine;
	private Level logLevel;
	
	private String path = "classpath:";
	
	public void destroy() throws Exception {
		iReportEngine.destroy();
		Platform.shutdown();
	}

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public IReportEngine getObject() throws Exception {
		EngineConfig config = new EngineConfig();
		config.getAppContext().put("spring", context);
		config.getAppContext().put("path", path);
//		config.setLogConfig( null != this._resolvedDirectory ? this._resolvedDirectory.getAbsolutePath() : null  , this.logLevel);
		
		try {
			Platform.startup( config );
		} catch (BirtException e) {
			throw new RuntimeException ("Could not start the Birt engine!", e) ;
		}
		
		IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		iReportEngine = factory.createReportEngine( config );
		return iReportEngine;
	}

	public Class<? extends IReportEngine> getObjectType() {
		return IReportEngine.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void setPath(String path) {
		Assert.isNotNull(path, "the path must be defined");
		this.path = path;
	}

	public void setLogLevel(Level logLevel) {
		Assert.isNotNull(path, "the log level must be defined");
		this.logLevel = logLevel;
	}

}
