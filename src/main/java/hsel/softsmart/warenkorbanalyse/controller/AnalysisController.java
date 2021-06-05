package hsel.softsmart.warenkorbanalyse.controller;

import hsel.softsmart.warenkorbanalyse.model.Result;
import hsel.softsmart.warenkorbanalyse.service.AnalysisService;
import hsel.softsmart.warenkorbanalyse.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping
    public String index(Model model) {
        Result result = analysisService.findLastResult();

        model.addAttribute("topDay", result.getTopDay());
        model.addAttribute("topTime", result.getTopTime());
        model.addAttribute("topProduct", result.getTopProduct());
        model.addAttribute("flopProduct", result.getFlopProduct());
        model.addAttribute("aprioriValues", result.getAprioriValues());

        return "analysis";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        File csvFile = FileUtil.parse(file);

        Instances csvData = analysisService.loadCSVData(csvFile);
        Instances arffData = analysisService.loadArffData(csvData);

        csvFile.delete();

        Result result = analysisService.processData(csvData, arffData);

        analysisService.saveResult(result);

        return "redirect:/analysis";
    }
}