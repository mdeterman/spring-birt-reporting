package ws.directweb.birt.spring.view;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.core.archive.FileArchiveWriter;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.impl.ScalarParameterDefn;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public abstract class ReportView {

	protected IReportEngine iReportEngine;
	
	protected abstract void renderMergedOutputModel(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
		renderMergedOutputModel(request, response);
	}
	
	protected Map<String, Object> discoverAndSetParameters(IReportRunnable runnable, HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		IGetParameterDefinitionTask task = runnable.getReportEngine().createGetParameterDefinitionTask(runnable);
		
		@SuppressWarnings("unchecked")
		Collection<ScalarParameterDefn> params = task.getParameterDefns(false);
		
		for(ScalarParameterDefn param: params) {
			Object obj = getRequestParameter(request, param.getName());
			switch (param.getDataType()) {
			case ScalarParameterDefn.TYPE_BOOLEAN:
				obj = Boolean.parseBoolean(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_TIME:
				// hh:mm:ss
				obj = java.sql.Time.valueOf(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_DATE_TIME:
				//  yyyy-mm-dd hh:mm:ss.fffffffff
				obj = java.sql.Timestamp.valueOf(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_DATE: 
				// yyyy-mm-dd
				obj = java.sql.Date.valueOf(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_DECIMAL:
				obj = Double.parseDouble(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_FLOAT: 
				obj = Float.parseFloat(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_INTEGER:
				obj = Integer.parseInt(obj.toString());
				break;
			case ScalarParameterDefn.TYPE_STRING: 
				// do nothing
				break;
			default: // TYPE_ANY
				// do nothing
				break;
			}
			
			map.put(param.getName(), obj);
		}
		return map;
	}
	
	private String getRequestParameter(HttpServletRequest request, String name) {
		return request.getParameter(name);
	}
	
//	protected void generateReportDocument(String reportName) throws Exception {
//		String path = (String) iReportEngine.getConfig().getAppContext().get("path");
//
//		if(getApplicationContext().getResource(path + reportName + ".rptdesign").exists()) {
//			System.out.println("rptdesign exists");
//		}
//		
//		if(getApplicationContext().getResource(path + reportName + ".rptdocument").exists()) {
//			System.out.println("rptdocument exists");
//		}
//		
//		Resource resource = getApplicationContext().getResource(path + reportName + ".rptdesign");
//		
//		IReportRunnable runnable = iReportEngine.openReportDesign(resource.getInputStream());
//		IRunTask runTask = iReportEngine.createRunTask(runnable);
//
//		runTask.validateParameters();
//		
//		FileArchiveWriter faw = new FileArchiveWriter(path + reportName + ".rptdocument" );
//		runTask.run(faw);
//	}
	
	protected ApplicationContext getApplicationContext() {
		return (ApplicationContext) iReportEngine.getConfig().getAppContext().get("spring");
	}
}
