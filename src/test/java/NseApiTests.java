import com.gaja.nse.config.Index;
import com.gaja.nse.config.NseAppConfig;
import com.gaja.nse.service.ScripClient;
import com.gaja.nse.utils.SecurityType;
import com.gaja.nse.utils.StockUtils;
import com.gaja.nse.vo.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NseAppConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NseApiTests {

    @Autowired
    private ScripClient client;

    private List<ScripData> allScripData;

    public void getAllScrips() throws IOException {
        this.allScripData = client.getAll(Index.NIFTY_50);
    }

    @Test
    public void fetchAllScrips() throws IOException {
        getAllScrips();
        long count = allScripData.stream().count();
        Assertions.assertEquals(50, count);
    }

    @Test
    public void fetchBhavCopy() throws IOException {
        OHLC volumeData = client.getTradeData("AUROPHARMA");
        System.out.println(volumeData);
        Assertions.assertTrue(volumeData!=null);
    }

    @Test
    public void fetchOHLCDataTataMotors() throws IOException {
        StockUtils.fetchOHLCHistory("TATAMOTORS", tatamotors -> Assertions.assertTrue(tatamotors.stream().count()>0));
    }

    @Test
    public void fetchBulkDealsForTataMotors() throws IOException {
        List<BulkDeal> tatamotors = client.getDeals("TATAMOTORS");
        Assertions.assertNotNull(tatamotors);
    }

    @Test
    public void fetchOptionsChartForTataMotors() throws IOException {
        List<OptionChain> tatamotors = client.getOptionChain("TATAMOTORS");
        Assertions.assertNotNull(tatamotors);
    }

    @Test
    public void fetchSecurityHistoryForTataMotors() throws IOException {
        List<DerivativeArchieve> tatamotors = StockUtils.fetchDerivativeHistory("TATAMOTORS", SecurityType.FUTSTK, null);
        Assertions.assertNotNull(tatamotors);
    }
    @Test
    public void fetchSecurityHistoryForNifty() throws IOException {
        List<DerivativeArchieve> tatamotors = StockUtils.fetchDerivativeHistory("NIFTY", SecurityType.FUTIDX, null);
        Assertions.assertNotNull(tatamotors);
    }

    @Test
    public void fetchOptionsHistoryForTataMotors() throws IOException {
        List<DerivativeArchieve> tatamotors = StockUtils.fetchDerivativeHistory("TATAMOTORS", SecurityType.OPTSTK, "CE");
        Assertions.assertNotNull(tatamotors);
    }
}
