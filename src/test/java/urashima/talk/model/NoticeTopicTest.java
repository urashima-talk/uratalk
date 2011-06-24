package urashima.talk.model;

import org.slim3.tester.AppEngineTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class NoticeTopicTest extends AppEngineTestCase {

    private NoticeTopic model = new NoticeTopic();

    @Test
    public void test() throws Exception {
        assertThat(model, is(notNullValue()));
    }
}
