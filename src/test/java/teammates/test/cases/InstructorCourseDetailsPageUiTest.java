package teammates.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import teammates.common.Common;
import teammates.common.datatransfer.DataBundle;
import teammates.test.driver.BackDoor;
import teammates.test.driver.BrowserInstance;
import teammates.test.driver.BrowserInstancePool;
import teammates.test.driver.EmailAccount;
import teammates.test.driver.NoAlertException;
import teammates.test.driver.TestProperties;

/**
 * Tests Instructor Course Details UI
 */
public class InstructorCourseDetailsPageUiTest extends BaseTestCase {
	private static BrowserInstance bi;
	private static DataBundle scn;
	
	private static String appUrl = TestProperties.inst().TEAMMATES_URL;

	@BeforeClass
	public static void classSetup() throws Exception {
		printTestClassHeader();
		
		startRecordingTimeForDataImport();
		String jsonString = Common.readFile(Common.TEST_DATA_FOLDER+"/InstructorCourseDetailsUiTest.json");
		scn = Common.getTeammatesGson().fromJson(jsonString, DataBundle.class);
		BackDoor.deleteInstructors(jsonString);
		String backDoorOperationStatus = BackDoor.persistNewDataBundle(jsonString);
		assertEquals(Common.BACKEND_STATUS_SUCCESS, backDoorOperationStatus);
		reportTimeForDataImport();
		
		bi = BrowserInstancePool.getBrowserInstance();

		bi.loginAdmin(TestProperties.inst().TEST_ADMIN_ACCOUNT, TestProperties.inst().TEST_ADMIN_PASSWORD);
		String link = appUrl+Common.PAGE_INSTRUCTOR_COURSE_DETAILS;
		link = Common.addParamToUrl(link,Common.PARAM_COURSE_ID,scn.courses.get("CCDetailsUiT.CS2104").id);
		link = Common.addParamToUrl(link,Common.PARAM_USER_ID,scn.instructors.get("teammates.test").id);
		bi.goToUrl(link);
	}
	
	@AfterClass
	public static void classTearDown() throws Exception {
		BrowserInstancePool.release(bi);
		printTestClassFooter();
	}
	
	@Test 
	public void runTestsInOrder() throws Exception{
		testInstructorCourseDetailsPageHTML();
		testInstructorCourseDetailsRemindStudent();
		//we put all tests here because we want this test to run last
		testInstructorCourseDetailsDeleteStudent();
	}
	
	public void testInstructorCourseDetailsPageHTML() throws Exception{
		
	
		______TS("default view");
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/instructorCourseDetailsPage.html");
		
		______TS("sort by team name");
		bi.click(bi.instructorCourseDetailSortByTeamName);
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/instructorCourseDetailsByTeam.html");
		
		______TS("sort by status");
		bi.click(bi.instructorCourseDetailSortByStatus);
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/instructorCourseDetailsByStatus.html");
		
		______TS("sort by student name");
		bi.click(bi.instructorCourseDetailSortByStudentName);
		bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/instructorCourseDetailsPage.html");
	}
	
	public void testInstructorCourseDetailsRemindStudent(){
		
		
		String studentName = scn.students.get("benny.tmms@CCDetailsUiT.CS2104").name;
		String studentEmail = scn.students.get("benny.tmms@CCDetailsUiT.CS2104").email;
		String otherStudentEmail = scn.students.get("charlie.tmms@CCDetailsUiT.CS2104").email;
		String registeredStudentEmail = scn.students.get("alice.tmms@CCDetailsUiT.CS2104").email;
		
		______TS("sending reminder to a single student to join course");
		
		bi.clickInstructorCourseDetailRemind(studentName);
		
		if(!TestProperties.inst().isLocalHost()){
			String key = BackDoor.getKeyForStudent(scn.courses.get("CCDetailsUiT.CS2104").id, studentEmail);
			bi.waitForEmail();
			assertEquals(key,EmailAccount.getRegistrationKeyFromGmail(studentEmail, TestProperties.inst().TEAMMATES_COMMON_PASSWORD_FOR_STUDENT_ACCOUNTS, scn.courses.get("CCDetailsUiT.CS2104").id));
		}
		
		______TS("sending reminder to all unregistered students to join course");
		
		bi.clickAndConfirm(bi.instructorCourseDetailRemindButton);
		if(!TestProperties.inst().isLocalHost()){
			bi.waitForEmail();
			
			//verify an unregistered student received reminder
			String key = BackDoor.getKeyForStudent(scn.courses.get("CCDetailsUiT.CS2104").id, otherStudentEmail);
			assertEquals(key,EmailAccount.getRegistrationKeyFromGmail(otherStudentEmail, TestProperties.inst().TEAMMATES_COMMON_PASSWORD_FOR_STUDENT_ACCOUNTS, scn.courses.get("CCDetailsUiT.CS2104").id));
			
			//verify a registered student did not receive a reminder
			assertEquals(null,EmailAccount.getRegistrationKeyFromGmail(registeredStudentEmail, TestProperties.inst().TEAMMATES_COMMON_PASSWORD_FOR_STUDENT_ACCOUNTS, scn.courses.get("CCDetailsUiT.CS2104").id));
		}
	}

	public void testInstructorCourseDetailsDeleteStudent() throws Exception{
		
		
		String studentName = scn.students.get("benny.tmms@CCDetailsUiT.CS2104").name;
		String studentEmail = scn.students.get("benny.tmms@CCDetailsUiT.CS2104").email;
		
		int studentRowId = bi.getStudentRowId(studentName);
		assertTrue(studentRowId!=-1);
		
		______TS("click and cancel");
		
		try{
			bi.clickInstructorCourseDetailStudentDeleteAndCancel(studentRowId);
			String student = BackDoor.getStudentAsJson(scn.courses.get("CCDetailsUiT.CS2104").id, studentEmail);
			if(isNullJSON(student)) {
				fail("Student was deleted when it's not supposed to be");
			}
		} catch (NoAlertException e){
			fail("No alert box when clicking delete button at course details page.");
		}

		______TS("click and confirm");
		
		try{
			bi.clickInstructorCourseDetailStudentDeleteAndConfirm(studentRowId);
			bi.verifyCurrentPageHTML(Common.TEST_PAGES_FOLDER+"/instructorCourseDetailsStudentDeleteSuccessful.html");
		} catch (NoAlertException e){
			fail("No alert box when clicking delete button at course details page.");
		}
	}
}