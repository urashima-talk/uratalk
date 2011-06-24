package urashima.talk.model;

import org.slim3.tester.AppEngineTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class TopicTest extends AppEngineTestCase {

    private Topic model = new Topic();

    @Test
    public void test() throws Exception {
        assertThat(model, is(notNullValue()));
    }
}
