package ws.directweb.birt.spring.view;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.core.runtime.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public class BirtView extends ReportView {

	private IRenderOption renderOptions ; 
	private String reportNameParameter = "ReportName";
	private String reportFormatParameter = "ReportFormat";
	
	public void setRenderOptions(IRenderOption renderOptions) {
		this.renderOptions = renderOptions;
	}
	
	public void setReportNameParameter(String reportNameParameter) {
		Assert.isNotNull(reportNameParameter, "the report name parameter must be defined");
		this.reportNameParameter = reportNameParameter;
	}

	public void setReportFormatParameter(String reportFormatParameter) {
		Assert.isNotNull(reportFormatParameter, "the reprot format parameter must be defined");
		this.reportFormatParameter = reportFormatParameter;
	}

	public void setiReportEngine(IReportEngine iReportEngine) {
		this.iReportEngine = iReportEngine;
	}

	protected void renderMergedOutputModel(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String reportName = request.getParameter(reportNameParameter);
		String format = request.getParameter(reportFormatParameter);
		ServletContext sc = request.getSession().getServletContext();
		if( format == null ){
			format="html";
		}
		
		IReportRunnable runnable = null;
		
		Resource resource = getApplicationContext().getResource(iReportEngine.getConfig().getAppContext().get("path") + reportName + ".rptdesign");
		runnable = iReportEngine.openReportDesign(resource.getInputStream());
		
//		IRunTask runTask = iReportEngine.createRunTask(runnable);
//		
////		runTask.get
//		IRenderTask iRenderTask = 
		
//		try {
//			generateReportDocument(reportName);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		IRunAndRenderTask runAndRenderTask = iReportEngine.createRunAndRenderTask(runnable);
		runAndRenderTask.setParameterValues(discoverAndSetParameters(runnable, request));
		
		response.setContentType(iReportEngine.getMIMEType(format));
		IRenderOption options =  null == this.renderOptions ? new RenderOption() : this.renderOptions;
		
		if(format.equalsIgnoreCase("html")){    
			HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
			htmlOptions.setOutputFormat("html");
			htmlOptions.setOutputStream(response.getOutputStream());
			
//			htmlOptions.setDisplayFilterIcon(false);
//			htmlOptions.setDisplayGroupIcon(false);
//			htmlOptions.setHTMLIndent(false);
//			htmlOptions.setHtmlPagination(false);
//			
//			htmlOptions.setHtmlTitle("MY TITLE");
//			htmlOptions.setMasterPageContent(false);

			
//			htmlOptions.setImageHandler(new HTMLServerImageHandler());
//			htmlOptions.setBaseImageURL(request.getContextPath()+"/images");
//			htmlOptions.setImageDirectory(sc.getRealPath("/images"));
			
//			htmlOptions.setHtmlPagination(false);
//			htmlOptions.setHtmlRtLFlag(false);
//			htmlOptions.setEmbeddable(false);
			
			runAndRenderTask.setRenderOption(htmlOptions);
		} else if(format.equalsIgnoreCase("pdf")){
			PDFRenderOption pdfOptions = new PDFRenderOption( options );
			pdfOptions.setOutputFormat("pdf");
			pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
			pdfOptions.setOutputStream(response.getOutputStream());
			runAndRenderTask.setRenderOption(pdfOptions);
		} else {
			String att  ="download."+format;
			String uReportName = reportName.toUpperCase(); 
			if( uReportName.endsWith(".RPTDESIGN") ){ 
				att = uReportName.replace(".RPTDESIGN", "."+format);
			}	
			response.setHeader(	"Content-Disposition", "attachment; filename=\"" + att + "\"" );
			options.setOutputStream(response.getOutputStream());
			options.setOutputFormat(format);
			runAndRenderTask.setRenderOption(options);
		}
//		EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST;
//		EngineConstants.APPCONTEXT_CLASSLOADER_KEY;
		
//		runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request);
		runAndRenderTask.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, this.getClass().getClassLoader());
		
		runAndRenderTask.run();	
		runAndRenderTask.close();
	}
	
}
