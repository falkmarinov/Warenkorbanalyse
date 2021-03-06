package hsel.softsmart.warenkorbanalyse.controller;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import hsel.softsmart.warenkorbanalyse.model.AprioriValue;
import hsel.softsmart.warenkorbanalyse.model.AssociatedProduct;
import hsel.softsmart.warenkorbanalyse.model.CustomerCsvEntry;
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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Webschnittstelle für {@code /path}.
 */
@Controller
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /**
     * Wird bei GET-Anfragen ausgeführt.
     *
     * Liest die letzten fünf Ergebnisse aus und koppelt diese an die View an.
     * Die View wird das letzte Ergebnis anzeigen.
     *
     * @param model Die Referenz zur View
     * @return Sendet dem Client die analysis View
     */
    @GetMapping
    public String index(Model model) {
        Result result = analysisService.findLastResult();
        List<Result> resultHistory = analysisService.resultHistory();

        model.addAttribute("result", result);
        model.addAttribute("resultHistory", resultHistory);

        Map<String, List<String>> aprioriPositions = new HashMap<>();
        for (AprioriValue aprioriValue : result.getAprioriValues()) {
            List<String> associatedProducts = new LinkedList<>();
            for (AssociatedProduct associatedProduct : aprioriValue.getAssociatedProducts()) {
                associatedProducts.add(associatedProduct.getAssociatedProduct());
            }
            aprioriPositions.put(aprioriValue.getProduct(), associatedProducts);
        }

        model.addAttribute("aprioriPositions", aprioriPositions);

        return "analysis";
    }

    /**
     * Wird bei POST-Anfragen ausgeführt.
     *
     * Liest die letzten fünf Ergebnisse aus und koppelt diese an die View an.
     * Die View wird das Ergebnis anzeigen, die ausgewählt wurde.
     *
     * @param id ErgebnisID die angefragt wird
     * @param model Die Referenz zur View
     * @return Sendet dem Client die analysis View
     */
    @PostMapping
    public String olderResult(@RequestParam("id") Long id, Model model) {
        Result result = analysisService.findResultById(id);
        List<Result> resultHistory = analysisService.resultHistory();

        model.addAttribute("result", result);
        model.addAttribute("resultHistory", resultHistory);

        Map<String, List<String>> aprioriPositions = new HashMap<>();
        for (AprioriValue aprioriValue : result.getAprioriValues()) {
            List<String> associatedProducts = new LinkedList<>();
            for (AssociatedProduct associatedProduct : aprioriValue.getAssociatedProducts()) {
                associatedProducts.add(associatedProduct.getAssociatedProduct());
            }
            aprioriPositions.put(aprioriValue.getProduct(), associatedProducts);
        }

        model.addAttribute("aprioriPositions", aprioriPositions);

        return "analysis";
    }

    /**
     * Verarbeitet die hochgeladene Datei und speichert die Ergebnisse in der Datenbank ab.
     *
     * @param file Hochgeladene Datei mit Kundeninformationen
     * @return Client wird an /analysis weitergeleitet
     * @throws IOException Zeigt IO Fehler an
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        File csvFile = FileUtil.parse(file);

        Instances csvData = analysisService.loadCSVData(csvFile);
        Instances arffData = analysisService.loadArffData(csvData);

        CSVReader csvReader = new CSVReader(new FileReader(csvFile));

        HeaderColumnNameMappingStrategy<CustomerCsvEntry> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
        mappingStrategy.setType(CustomerCsvEntry.class);

        CsvToBean<CustomerCsvEntry> csvToBean = new CsvToBean<>();
        csvToBean.setCsvReader(csvReader);
        csvToBean.setMappingStrategy(mappingStrategy);

        List<CustomerCsvEntry> csvEntries = csvToBean.parse();

        Result result = analysisService.processData(csvEntries, csvData, arffData);

        csvFile.delete();

        analysisService.saveResult(result);

        return "redirect:/analysis";
    }
}
