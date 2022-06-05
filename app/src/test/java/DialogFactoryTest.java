import com.kabouzeid.gramophone.dialogs.DialogFactory;

import org.junit.Assert;
import org.junit.Test;

public class DialogFactoryTest {

    /**
     * Purpose: to confirm Singleton Pattern
     * Input: getInstance dialogFactory1:()->DialogFactory, dialogFactory1:()->DialogFactory
     * Expected:
     *      dialogFactory1 = dialogFactory2
     */
    @Test
    public void testGetInstance() {
        System.out.println("test start");
        DialogFactory dialogFactory1 = DialogFactory.getInstance();
        DialogFactory dialogFactory2 = DialogFactory.getInstance();
        Assert.assertEquals(dialogFactory1,dialogFactory2);
    }
}