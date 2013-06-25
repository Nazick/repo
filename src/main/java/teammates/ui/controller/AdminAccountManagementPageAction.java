package teammates.ui.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teammates.common.Common;
import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.logic.GateKeeper;

public class AdminAccountManagementPageAction extends Action {

	@Override
	protected ActionResult execute() throws EntityDoesNotExistException,
			InvalidParametersException {
		
		new GateKeeper().verifyAdminLoggedIn();
		
		AdminAccountManagementPageData data = new AdminAccountManagementPageData(account);
		
		List<InstructorAttributes> allInstructorsList = logic.getAllInstructors();
		List<AccountAttributes> allInstructorAccountsList = logic.getInstructorAccounts();
		
		data.instructorCoursesTable = new HashMap<String, ArrayList<InstructorAttributes>>();
		data.instructorAccountsTable = new HashMap<String, AccountAttributes>();
		
		for(AccountAttributes acc : allInstructorAccountsList){
			data.instructorAccountsTable.put(acc.googleId, acc);
		}
		
		for(InstructorAttributes instructor : allInstructorsList){
			ArrayList<InstructorAttributes> courseList = data.instructorCoursesTable.get(instructor.googleId);
			if (courseList == null){
				courseList = new ArrayList<InstructorAttributes>();
				data.instructorCoursesTable.put(instructor.googleId, courseList);
			}
			courseList.add(instructor);
		}
			
		statusToAdmin = "Admin Account Management Page Load<br>" + 
				"<span class=\"bold\">Total Instructors:</span> " + 
				data.instructorAccountsTable.size();
		
		return createShowPageResult(Common.JSP_ADMIN_ACCOUNT_MANAGEMENT, data);
	}

}