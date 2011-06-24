package urashima.talk.model;

import org.slim3.tester.AppEngineTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class NoticeCommentTest extends AppEngineTestCase {

    private NoticeComment model = new NoticeComment();

    @Test
    public void test() throws Exception {
        assertThat(model, is(notNullValue()));
    }
}
