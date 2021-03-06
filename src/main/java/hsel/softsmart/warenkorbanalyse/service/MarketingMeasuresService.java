package hsel.softsmart.warenkorbanalyse.service;

import hsel.softsmart.warenkorbanalyse.model.MarketingMeasure;

import java.util.List;

public interface MarketingMeasuresService {
    List<MarketingMeasure> getAllMarketingMeasures();

    void deleteMarketingMeasure(long id);

    void updateMarketingMeasure(long id, String measure);

    void saveMarketingMeasure(MarketingMeasure marketingMeasure);
}
