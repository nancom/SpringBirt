package com.spring.birt.core;

import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.IHTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.view.AbstractView;
import org.eclipse.birt.report.engine.api.EngineConstants;
import java.io.*;
import org.springframework.util.Assert;

public class BirtView extends AbstractView {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PARAM_ISNULL = "__isnull";
	public static final String UTF_8_ENCODE = "UTF-8"; 

	private IReportEngine birtEngine;
	private String reportNameRequestParameter = "ReportName" ;
    private String reportFormatRequestParameter = "ReportFormat";

    private IRenderOption renderOptions ;

	public void setRenderOptions(IRenderOption ro) { 
		this.renderOptions = ro;
	} 
	
	public void setReportFormatRequestParameter( String rf ){ 
		Assert.hasText( rf , "the report format parameter must not be null") ;
		this.reportFormatRequestParameter = rf ;
	}

	public void setReportNameRequestParameter ( String rn ) { 
		Assert.hasText( rn , "the reportNameRequestParameter must not be null") ;
		this.reportNameRequestParameter = rn ; 
	}

	protected void renderMergedOutputModel(
			Map map, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String reportName = request.getParameter( this.reportNameRequestParameter );
		String format = request.getParameter( this.reportFormatRequestParameter );

        Enumeration enumeration = request.getParameterNames();

        //Check Parameter
        while(enumeration.hasMoreElements()){
            Object obj = enumeration.nextElement();
            logger.debug("parameters:{} / {}",obj,request.getParameter(obj.toString()));
        }

        Locale.setDefault(request.getLocale());
        logger.debug("locale:{}",Locale.getDefault());

        ServletContext sc = request.getSession().getServletContext();
		if( format == null ){
			format="html";
		}

		IReportRunnable runnable = null;
        /*
        *Need to refactoring to properties file
        * */
        logger.debug("Real Path: {}",sc.getRealPath("/WEB-INF/classes/Reports"));
		runnable = birtEngine.openReportDesign( sc.getRealPath("/WEB-INF/classes/Reports")+"/"+reportName );
		IRunAndRenderTask runAndRenderTask = birtEngine.createRunAndRenderTask(runnable);
        runAndRenderTask.setParameterValues(discoverAndSetParameters( runnable, request ));

        HashMap hMap = runAndRenderTask.getParameterValues();
        logger.debug("Paramater Map: {}",hMap);

        response.setContentType( birtEngine.getMIMEType( format ));
		IRenderOption options =  null == this.renderOptions ? new RenderOption() : this.renderOptions;
        if( format.equalsIgnoreCase("html")){
			HTMLRenderOption htmlOptions = new HTMLRenderOption( options);
			htmlOptions.setOutputFormat("html");
			htmlOptions.setOutputStream(response.getOutputStream());
			htmlOptions.setImageHandler(new HTMLServerImageHandler());
			htmlOptions.setBaseImageURL(request.getContextPath()+"/images");
			htmlOptions.setImageDirectory(sc.getRealPath("/images"));
			runAndRenderTask.setRenderOption(htmlOptions);

		}else if( format.equalsIgnoreCase("pdf") ){
			PDFRenderOption pdfOptions = new PDFRenderOption( options );
			logger.debug("-= Before set report format =-");
			pdfOptions.setOutputFormat("pdf");
			logger.debug("-= After set report format =-");
			pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
			logger.debug("-= After set report Option  =-");
			pdfOptions.setOutputStream(response.getOutputStream());
			logger.debug("-= After set Output Stream =-");
			runAndRenderTask.setRenderOption(pdfOptions);
			logger.debug("-= After set render option =-");
		}else{

			String att  ="download."+format;
			String uReportName = reportName.toUpperCase(); 
			if( uReportName.endsWith(".RPTDESIGN") ){ 
				att = uReportName.replace(".RPTDESIGN", "."+format);
			}	

			try{
				// Create file 
				FileWriter fstream = new FileWriter(System.getProperty("java.io.tmpdir")+"birt-out.txt");
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("Hello Java " + format + "--" + birtEngine.getMIMEType( format ));
				//Close the output stream
				out.close();
			}catch (Exception e){//Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}

			response.setHeader(	"Content-Disposition", "attachment; filename=\"" + att + "\"" );
			options.setOutputStream(response.getOutputStream());
			options.setOutputFormat(format);
			runAndRenderTask.setRenderOption(options);
		}
        Locale.setDefault(Locale.US);
		runAndRenderTask.getAppContext().put( EngineConstants.APPCONTEXT_BIRT_VIEWER_HTTPSERVET_REQUEST, request );
		runAndRenderTask.run();	
		runAndRenderTask.close();

        logger.debug("-= Finished  =-");
	}
	protected HashMap discoverAndSetParameters( IReportRunnable report, HttpServletRequest request ) throws Exception{

		HashMap<String, Object> parms = new HashMap<String, Object>();  
		IGetParameterDefinitionTask task = birtEngine.createGetParameterDefinitionTask( report );

		@SuppressWarnings("unchecked")
		Collection<IParameterDefnBase> params = task.getParameterDefns( true );
		Iterator<IParameterDefnBase> iter = params.iterator( );
		while ( iter.hasNext( ) )
		{
			IParameterDefnBase param = (IParameterDefnBase) iter.next( );

			IScalarParameterDefn scalar = (IScalarParameterDefn) param;
			if( request.getParameter(param.getName()) != null ){
				parms.put( param.getName(), getParamValueObject( request, scalar));
			}
		}
		task.close();
		return parms;
	}
	protected Object getParamValueObject( HttpServletRequest request,
			IScalarParameterDefn parameterObj ) throws Exception
			{
		String paramName = parameterObj.getName( );
		String format = parameterObj.getDisplayFormat( );
		if ( doesReportParameterExist( request, paramName ) )
		{
			ReportParameterConverter converter = new ReportParameterConverter(
					format, request.getLocale( ) );
			// Get value from http request
			String paramValue = getReportParameter( request,
					paramName, null );
			return converter.parse( paramValue, parameterObj.getDataType( ) );
		}
		return null;
			}
	public static String getReportParameter( HttpServletRequest request,
			String name, String defaultValue )
	{
		assert request != null && name != null;

		String value = getParameter( request, name );
		if ( value == null || value.length( ) <= 0 ) // Treat
			// it as blank value.
		{
			value = ""; //$NON-NLS-1$
		}

		Map paramMap = request.getParameterMap( );
		if ( paramMap == null || !paramMap.containsKey( name ) )
		{
			value = defaultValue;
		}

		Set nullParams = getParameterValues( request, PARAM_ISNULL );

		if ( nullParams != null && nullParams.contains( name ) )
		{
			value = null;
		}

		return value;
	}
	public static boolean doesReportParameterExist( HttpServletRequest request,
			String name )
	{
		assert request != null && name != null;

		boolean isExist = false;

		Map paramMap = request.getParameterMap( );
		if ( paramMap != null )
		{
			isExist = ( paramMap.containsKey( name ) );
		}
		Set nullParams = getParameterValues( request, PARAM_ISNULL );
		if ( nullParams != null && nullParams.contains( name ) )
		{
			isExist = true;
		}

		return isExist;
	}
	public static String getParameter( HttpServletRequest request,
			String parameterName )
	{

		if ( request.getCharacterEncoding( ) == null )
		{
			try
			{
				request.setCharacterEncoding( UTF_8_ENCODE );
			}
			catch ( UnsupportedEncodingException e )
			{
			}
		}
		return request.getParameter( parameterName );
	}

	//allows setting parameter values to null using __isnull
	public static Set getParameterValues( HttpServletRequest request,
			String parameterName )
	{
		Set<String> parameterValues = null;
		String[] parameterValuesArray = request.getParameterValues( parameterName );

		if ( parameterValuesArray != null )
		{
			parameterValues = new LinkedHashSet<String>( );

			for ( int i = 0; i < parameterValuesArray.length; i++ )
			{
				parameterValues.add( parameterValuesArray[i] );
			}
		}

		return parameterValues;
	}

	public void setBirtEngine(IReportEngine birtEngine) {
		this.birtEngine = birtEngine;
	}

}
