package com.fowlart.main;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

public class AzureConfigurationReaderTest {

    //@Test
    public void readConfigurationFromAzure() {

        var readOnlyConnectionString = "Endpoint=https://tg-bot-config-store.azconfig.io;Id=zUCE;Secret=Qs3YOoKnhD6R1L/lUp7iwplRUR3izI34ZRXA9sHTC5E=";

        ConfigurationClient configurationClient = new ConfigurationClientBuilder().connectionString(readOnlyConnectionString).buildClient();

        ConfigurationSetting retrievedSetting = configurationClient.getConfigurationSetting("test_key", null);

        Assert.isTrue("test_value".equals(retrievedSetting.getValue()), "Expected value not received!");

    }
}
