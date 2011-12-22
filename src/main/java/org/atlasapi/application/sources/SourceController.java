package org.atlasapi.application.sources;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationManager;
import org.atlasapi.application.www.ApplicationModelBuilder;
import org.atlasapi.media.entity.Publisher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.model.ModelBuilder;
import com.metabroadcast.common.model.SimpleModelList;
import com.metabroadcast.common.query.Selection;

@Controller
public class SourceController {

    private final ApplicationManager appManager;

    private final SourceIdCodec idCodec;
    private final SourceModelBuilder sourceModelBuilder;

    public SourceController(ApplicationManager appManager) {
        this.appManager = appManager;
        this.idCodec = new SourceIdCodec(new SubstitutionTableNumberCodec());
        this.sourceModelBuilder = new SourceModelBuilder(idCodec);
    }
    
    @RequestMapping(value="/admin/sources/{id}/applications", method=RequestMethod.GET)
    public String applicationsForSource(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
    
        Maybe<Publisher> decodedPublisher = idCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            response.setStatus(HttpStatusCode.NOT_FOUND.code());
            response.setContentLength(0);
            return null;
        }
        
        Publisher publisher = decodedPublisher.requireValue();
        
        Selection selection = Selection.builder().build(request);
        
        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder , selection.applyTo(appManager.applicationsFor(publisher))));
        model.put("source", sourceModelBuilder.build(publisher));
        
        return "applications/source";
    }

    @RequestMapping(value="/admin/sources/{id}/applications/approved", method=RequestMethod.POST)
    public String approveApplication(Map<String,Object> model, HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
        
        Maybe<Publisher> decodedPublisher = idCodec.decode(id);
        
        if (decodedPublisher.isNothing()) {
            return sendError(response, HttpStatusCode.NOT_FOUND.code());
        }
        
        Publisher publisher = decodedPublisher.requireValue();
        
        Application application = appManager.approvePublisher(request.getParameter("application"), publisher);

        ModelBuilder<Application> applicationModelBuilder = new ApplicationModelBuilder(new SourceSpecificApplicationConfigurationModelBuilder(publisher));
        model.put("applications", SimpleModelList.fromBuilder(applicationModelBuilder, ImmutableList.of(application)));
        model.put("source", sourceModelBuilder.build(publisher));
        
        return "applications/source";
    }

    public String sendError(HttpServletResponse response, final int code) {
        response.setStatus(code);
        response.setContentLength(0);
        return null;
    }
    
}
