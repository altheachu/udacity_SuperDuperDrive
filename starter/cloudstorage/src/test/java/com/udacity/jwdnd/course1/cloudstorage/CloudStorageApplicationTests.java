package com.udacity.jwdnd.course1.cloudstorage;

import com.udacity.jwdnd.course1.cloudstorage.entity.Credential;
import com.udacity.jwdnd.course1.cloudstorage.entity.Note;
import com.udacity.jwdnd.course1.cloudstorage.entity.User;
import com.udacity.jwdnd.course1.cloudstorage.services.CredentialService;
import com.udacity.jwdnd.course1.cloudstorage.services.EncryptionService;
import com.udacity.jwdnd.course1.cloudstorage.services.UserService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.File;

import static java.lang.Thread.sleep;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CloudStorageApplicationTests {

	@Autowired
	private EncryptionService encryptionService;
	@Autowired
	private CredentialService credentialService;

	@Autowired
	private UserService userService;

	@LocalServerPort
	private int port;
	private WebDriver driver;

	private SignupPage signupPage;

	private LoginPage loginPage;

	private HomePage homePage;

	private NotePage notePage;

	private CredentialPage credentialPage;

	private ResultPage resultPage;

	@BeforeAll
	static void beforeAll() {
		WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	public void beforeEach() {
		this.driver = new ChromeDriver();
	}

	@AfterEach
	public void afterEach() {
		if (this.driver != null) {
			driver.quit();
		}
	}

	/*verifies that an unauthorized user can only access the login and signup pages.*/
	@Test
	public void getPagesByUnauthorizedUser() {
		// login page
		driver.get("http://localhost:" + this.port + "/login");
		Assertions.assertEquals("Login", driver.getTitle());
		// signup page
		driver.get("http://localhost:" + this.port + "/signup");
		Assertions.assertEquals("Sign Up", driver.getTitle());
		// home page
		driver.get("http://localhost:" + this.port + "/home");
		Assertions.assertNotEquals("Home", driver.getTitle());
		// result page
		driver.get("http://localhost:" + this.port + "/result");
		Assertions.assertNotEquals("Result", driver.getTitle());
	}
	/*signs up a new user, logs in, verifies that the home page is accessible,
	logs out, and verifies that the home page is no longer accessible.*/
	@Test
	public void user_happy_path(){
		User user = CloudStorageApplicationTests.getMockUserInfo(1);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		Assertions.assertEquals("http://localhost:" + this.port + "/login", driver.getCurrentUrl());
		Assertions.assertTrue(driver.findElement(By.id("success-msg")).getText().contains("You successfully signed up!"));
		// log in
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		Assertions.assertEquals("http://localhost:" + this.port + "/home", driver.getCurrentUrl());
		// logout
		homePage = new HomePage(driver);
		homePage.logout();
		Assertions.assertEquals("http://localhost:" + this.port + "/login", driver.getCurrentUrl());
		driver.get("http://localhost:" + this.port + "/home");
		Assertions.assertNotEquals("http://localhost:" + this.port + "/home", driver.getCurrentUrl());
	}

	/*creates a note, and verifies it is displayed*/
	@Test
	public void create_notes_happy_path(){
		User user = CloudStorageApplicationTests.getMockUserInfo(2);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		//login in
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		// navigate to notes tab
		homePage = new HomePage(driver);
		homePage.navToNotes();
		notePage = new NotePage(driver);
		WebDriverWait webDriverWait = new WebDriverWait(driver,5);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openNoteModal")));
		notePage.openModal();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-title")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-description")));
		// create note
		Note note = CloudStorageApplicationTests.getMockNoteInfo();
		notePage.createOrUpdateNote(note);
		// back from result
		resultPage = new ResultPage(driver);
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to notes tab again
		homePage.navToNotes();
		// wait util element display
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"userTable\"]/tbody/tr[1]/th")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"userTable\"]/tbody/tr[1]/td[2]")));
		// check display
		Assertions.assertEquals(note.getNoteTitle(),notePage.getNoteTitleDisplay());
		Assertions.assertEquals(note.getNoteDescription(),notePage.getNoteDescriptionDisplay());
		homePage.logout();
	}

	/*edits an existing note and verifies that the changes are displayed.*/
	@Test
	public void edit_notes_happy_path(){
		User user = CloudStorageApplicationTests.getMockUserInfo(3);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		//login in
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		// navigate to notes tab
		homePage = new HomePage(driver);
		homePage.navToNotes();
		notePage = new NotePage(driver);
		WebDriverWait webDriverWait = new WebDriverWait(driver,5);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openNoteModal")));
		notePage.openModal();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-title")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-description")));
		// create note
		Note note = CloudStorageApplicationTests.getMockNoteInfo();
		notePage.createOrUpdateNote(note);
		// back from result
		resultPage = new ResultPage(driver);
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to notes tab again
		homePage.navToNotes();
		// open edit modal
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"userTable\"]/tbody/tr[1]/td[1]/button")));
		notePage.getNoteEditModal();
		// edit note
		Note note2 = CloudStorageApplicationTests.getMockNoteInfo2();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-title")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-description")));
		notePage.clearPreviousInput();
		notePage.createOrUpdateNote(note2);
		// back from result
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to notes tab again
		homePage.navToNotes();
		// wait util element display
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"userTable\"]/tbody/tr[1]/th")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"userTable\"]/tbody/tr[1]/td[2]")));
		// check display after note edited
		Assertions.assertEquals(note2.getNoteTitle(),notePage.getNoteTitleDisplay());
		Assertions.assertEquals(note2.getNoteDescription(),notePage.getNoteDescriptionDisplay());
		homePage.logout();
	}

	/*Write a test that deletes a note and verifies that the note is no longer displayed.*/
	@Test
	public void delete_notes_happy_path(){
		User user = CloudStorageApplicationTests.getMockUserInfo(4);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		//login in
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		// navigate to notes tab
		homePage = new HomePage(driver);
		homePage.navToNotes();
		notePage = new NotePage(driver);
		WebDriverWait webDriverWait = new WebDriverWait(driver,5);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openNoteModal")));
		notePage.openModal();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-title")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("note-description")));
		// create note
		Note note = CloudStorageApplicationTests.getMockNoteInfo();
		notePage.createOrUpdateNote(note);
		// back from result
		resultPage = new ResultPage(driver);
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to notes tab again
		homePage.navToNotes();
		// wait util element display
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"userTable\"]/tbody/tr[1]/td[1]/a")));
		// delete note
		notePage.deleteNote();
		// back from result
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to note page
		homePage.navToNotes();
		// wait for a moment
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		// check element disappear
		Exception e1 = Assertions.assertThrows(Exception.class, ()->notePage.getNoteTitleDisplay());
		Assertions.assertTrue(e1.getMessage().contains("no such element"));
		Exception e2 = Assertions.assertThrows(Exception.class, ()->notePage.getNoteDescriptionDisplay());
		Assertions.assertTrue(e2.getMessage().contains("no such element"));
		homePage.logout();
	}

	/*creates a set of credentials, verifies that they are displayed, and verifies that the displayed password is encrypted.*/
	@Test
	public void create_credential_happy_path(){
		User user = CloudStorageApplicationTests.getMockUserInfo(5);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		Integer userId = userService.findUserIdByUsername(user.getUsername());
		//login in
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		// navigate to credential tab
		homePage = new HomePage(driver);
		homePage.navToCredential();
		// open modal
		credentialPage = new CredentialPage(driver);
		WebDriverWait webDriverWait = new WebDriverWait(driver,5);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openCredentialModal")));
		credentialPage.openModal();
		// create credential
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-url")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-username")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-password")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credentialSave")));

		Credential credential = CloudStorageApplicationTests.getMockCredentialInfo1();
		credentialPage.createOrUpdate(credential);
		// return homepage from resultpage
		resultPage = new ResultPage(driver);
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to credential tab
		homePage.navToCredential();
		// get encrypted password
		String key = credentialService.findKeyById(userId);
		String encryptedPassword = encryptionService.encryptValue(credential.getPassword(), key);
		// compare actual and expected value
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/th")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[2]")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[3]")));
		Credential credentialActual = credentialPage.getCredentialAtHomePage();
		Assertions.assertEquals(credential.getUrl(), credentialActual.getUrl());
		Assertions.assertEquals(credential.getUsername(), credentialActual.getUsername());
		Assertions.assertEquals(encryptedPassword, credentialActual.getPassword());
		homePage.logout();
	}

	/*views an existing set of credentials, verifies that the viewable password is unencrypted, edits the credentials, and verifies that the changes are displayed.*/
	@Test
	public void edit_credential_happy_path(){
		User user = CloudStorageApplicationTests.getMockUserInfo(6);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		Integer userId = userService.findUserIdByUsername(user.getUsername());
		//login in
		WebDriverWait webDriverWait = new WebDriverWait(driver,5);
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		// navigate to credential tab
		homePage = new HomePage(driver);
		homePage.navToCredential();
		// open modal
		credentialPage = new CredentialPage(driver);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openCredentialModal")));
		credentialPage.openModal();
		// create credential
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-url")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-username")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-password")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credentialSave")));
		Credential credential = CloudStorageApplicationTests.getMockCredentialInfo1();
		credentialPage.createOrUpdate(credential);
		// return homepage from resultpage
		resultPage = new ResultPage(driver);
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to credential tab
		homePage.navToCredential();
		// open modal
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[1]/button")));
		credentialPage.startEdit();
		// verify url, username, password is unencrypted
		Assertions.assertEquals(credential.getUrl(), credentialPage.getExistingInfoInModal().getUrl());
		Assertions.assertEquals(credential.getUsername(), credentialPage.getExistingInfoInModal().getUsername());
		Assertions.assertEquals(credential.getPassword(), credentialPage.getExistingInfoInModal().getPassword());
		// update credential
		Credential newCredential = CloudStorageApplicationTests.getMockCredentialInfo2();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-url")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-username")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-password")));
		credentialPage.clearPreviousInput();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credentialSave")));
		credentialPage.createOrUpdate(newCredential);
		// return homepage from resultpage
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to credential tab
		homePage.navToCredential();
		// get encrypted password
		String key = credentialService.findKeyById(userId);
		String encryptedPassword = encryptionService.encryptValue(newCredential.getPassword(), key);
		// compare actual and expected value
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/th")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[2]")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[3]")));
		Credential credentialActual = credentialPage.getCredentialAtHomePage();
		Assertions.assertEquals(newCredential.getUrl(), credentialActual.getUrl());
		Assertions.assertEquals(newCredential.getUsername(), credentialActual.getUsername());
		Assertions.assertEquals(encryptedPassword, credentialActual.getPassword());
		// open modal again
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[1]/button")));
		credentialPage.startEdit();
		// verify url, username, password is unencrypted again
		Assertions.assertEquals(newCredential.getUrl(), credentialPage.getExistingInfoInModal().getUrl());
		Assertions.assertEquals(newCredential.getUsername(), credentialPage.getExistingInfoInModal().getUsername());
		Assertions.assertEquals(newCredential.getPassword(), credentialPage.getExistingInfoInModal().getPassword());
		// cancel modal
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credentialClose")));
		credentialPage.cancelEdit();
		// logout
		homePage.logout();
	}

	/*deletes an existing set of credentials and verifies that the credentials are no longer displayed.*/
	@Test
	public void delete_credential_happy_path() {
		User user = CloudStorageApplicationTests.getMockUserInfo(7);
		// sign up
		signupPage = new SignupPage(driver);
		driver.get("http://localhost:" + this.port + "/signup");
		signupPage.signup(user);
		Integer userId = userService.findUserIdByUsername(user.getUsername());
		//login in
		loginPage = new LoginPage(driver);
		loginPage.login(user);
		// navigate to credential tab
		homePage = new HomePage(driver);
		homePage.navToCredential();
		// open modal
		credentialPage = new CredentialPage(driver);
		WebDriverWait webDriverWait = new WebDriverWait(driver,5);
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("openCredentialModal")));
		credentialPage.openModal();
		// create credential
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-url")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-username")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credential-password")));
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("credentialSave")));
		Credential credential = CloudStorageApplicationTests.getMockCredentialInfo1();
		credentialPage.createOrUpdate(credential);
		// return homepage from resultpage
		resultPage = new ResultPage(driver);
		resultPage.goBackToHomeFromSuccessMsg();
		// navigate to credential tab
		homePage.navToCredential();
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"credentialTable\"]/tbody/tr[1]/td[1]/a")));
		credentialPage.deleteCredential();
		resultPage.goBackToHomeFromSuccessMsg();
		homePage.navToCredential();
		// wait for loading
		try {
			sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		// check element disappear
		Exception e1 = Assertions.assertThrows(Exception.class, ()->credentialPage.getCredentialAtHomePage().getUrl());
		Assertions.assertTrue(e1.getMessage().contains("no such element"));
		Exception e2 = Assertions.assertThrows(Exception.class, ()->credentialPage.getCredentialAtHomePage().getUsername());
		Assertions.assertTrue(e2.getMessage().contains("no such element"));
		Exception e3 = Assertions.assertThrows(Exception.class, ()->credentialPage.getCredentialAtHomePage().getPassword());
		Assertions.assertTrue(e3.getMessage().contains("no such element"));
		homePage.logout();
	}

	@Test
	public void getLoginPage() {
		driver.get("http://localhost:" + this.port + "/login");
		Assertions.assertEquals("Login", driver.getTitle());
	}

	/**
	 * PLEASE DO NOT DELETE THIS method.
	 * Helper method for Udacity-supplied sanity checks.
	 **/
	private void doMockSignUp(String firstName, String lastName, String userName, String password){
		// Create a dummy account for logging in later.

		// Visit the sign-up page.
		WebDriverWait webDriverWait = new WebDriverWait(driver, 2);
		driver.get("http://localhost:" + this.port + "/signup");
		webDriverWait.until(ExpectedConditions.titleContains("Sign Up"));
		
		// Fill out credentials
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputFirstName")));
		WebElement inputFirstName = driver.findElement(By.id("inputFirstName"));
		inputFirstName.click();
		inputFirstName.sendKeys(firstName);

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputLastName")));
		WebElement inputLastName = driver.findElement(By.id("inputLastName"));
		inputLastName.click();
		inputLastName.sendKeys(lastName);

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputUsername")));
		WebElement inputUsername = driver.findElement(By.id("inputUsername"));
		inputUsername.click();
		inputUsername.sendKeys(userName);

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputPassword")));
		WebElement inputPassword = driver.findElement(By.id("inputPassword"));
		inputPassword.click();
		inputPassword.sendKeys(password);

		// Attempt to sign up.
		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("buttonSignUp")));
		WebElement buttonSignUp = driver.findElement(By.id("buttonSignUp"));
		buttonSignUp.click();

		/* Check that the sign up was successful. 
		// You may have to modify the element "success-msg" and the sign-up 
		// success message below depending on the rest of your code.
		*/
		Assertions.assertTrue(driver.findElement(By.id("success-msg")).getText().contains("You successfully signed up!"));

	}

	
	
	/**
	 * PLEASE DO NOT DELETE THIS method.
	 * Helper method for Udacity-supplied sanity checks.
	 **/
	private void doLogIn(String userName, String password)
	{
		// Log in to our dummy account.
		driver.get("http://localhost:" + this.port + "/login");
		WebDriverWait webDriverWait = new WebDriverWait(driver, 2);

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputUsername")));
		WebElement loginUserName = driver.findElement(By.id("inputUsername"));
		loginUserName.click();
		loginUserName.sendKeys(userName);

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inputPassword")));
		WebElement loginPassword = driver.findElement(By.id("inputPassword"));
		loginPassword.click();
		loginPassword.sendKeys(password);

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-button")));
		WebElement loginButton = driver.findElement(By.id("login-button"));
		loginButton.click();

		webDriverWait.until(ExpectedConditions.titleContains("Home"));

	}

	/**
	 * PLEASE DO NOT DELETE THIS TEST. You may modify this test to work with the 
	 * rest of your code. 
	 * This test is provided by Udacity to perform some basic sanity testing of 
	 * your code to ensure that it meets certain rubric criteria. 
	 * 
	 * If this test is failing, please ensure that you are handling redirecting users 
	 * back to the login page after a succesful sign up.
	 * Read more about the requirement in the rubric: 
	 * https://review.udacity.com/#!/rubrics/2724/view 
	 */
	@Test
	public void testRedirection() {
		// Create a test account
		doMockSignUp("Redirection","Test","RT","123");
		// Check if we have been redirected to the log in page.
		Assertions.assertEquals("http://localhost:" + this.port + "/login", driver.getCurrentUrl());
	}

	/**
	 * PLEASE DO NOT DELETE THIS TEST. You may modify this test to work with the 
	 * rest of your code. 
	 * This test is provided by Udacity to perform some basic sanity testing of 
	 * your code to ensure that it meets certain rubric criteria. 
	 * 
	 * If this test is failing, please ensure that you are handling bad URLs 
	 * gracefully, for example with a custom error page.
	 * 
	 * Read more about custom error pages at: 
	 * https://attacomsian.com/blog/spring-boot-custom-error-page#displaying-custom-error-page
	 */
	@Test
	public void testBadUrl() {
		// Create a test account
		doMockSignUp("URL","Test","UT","123");
		doLogIn("UT", "123");
		
		// Try to access a random made-up URL.
		driver.get("http://localhost:" + this.port + "/some-random-page");
		Assertions.assertFalse(driver.getPageSource().contains("Whitelabel Error Page"));
	}


	/**
	 * PLEASE DO NOT DELETE THIS TEST. You may modify this test to work with the 
	 * rest of your code. 
	 * This test is provided by Udacity to perform some basic sanity testing of 
	 * your code to ensure that it meets certain rubric criteria. 
	 * 
	 * If this test is failing, please ensure that you are handling uploading large files (>1MB),
	 * gracefully in your code. 
	 * 
	 * Read more about file size limits here: 
	 * https://spring.io/guides/gs/uploading-files/ under the "Tuning File Upload Limits" section.
	 */
	@Test
	public void testLargeUpload() {
		// Create a test account
		doMockSignUp("Large File","Test","LFT","123");
		doLogIn("LFT", "123");

		// Try to upload an arbitrary large file
		WebDriverWait webDriverWait = new WebDriverWait(driver, 2);
		String fileName = "upload5m.zip";

		webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fileUpload")));
		WebElement fileSelectButton = driver.findElement(By.id("fileUpload"));
		fileSelectButton.sendKeys(new File(fileName).getAbsolutePath());

		WebElement uploadButton = driver.findElement(By.id("uploadButton"));
		uploadButton.click();
		try {
			webDriverWait.until(ExpectedConditions.presenceOfElementLocated(By.id("success")));
		} catch (org.openqa.selenium.TimeoutException e) {
			System.out.println("Large File upload failed");
		}
		Assertions.assertFalse(driver.getPageSource().contains("HTTP Status 403 – Forbidden"));

	}

	private static User getMockUserInfo(int i){

		User user = new User();
		user.setFirstname("John");
		user.setLastname("Chen");
		user.setPassword("1234");
		switch(i) {
			case 1:
				user.setUsername("t1111");
				break;
			case 2:
				user.setUsername("t1112");
				break;
			case 3:
				user.setUsername("t1113");
				break;
			case 4:
				user.setUsername("t1114");
				break;
			case 5:
				user.setUsername("t1115");
				break;
			case 6:
				user.setUsername("t1116");
				break;
			case 7:
				user.setUsername("t1117");
				break;
		}

		return user;
	}

	private static Note getMockNoteInfo(){
		Note note = new Note();
		note.setNoteTitle("testNoteTitle");
		note.setNoteDescription("testNoteDescription");
		return note;
	}

	private static Note getMockNoteInfo2(){
		Note note = new Note();
		note.setNoteTitle("editNoteTitle");
		note.setNoteDescription("editNoteDescription");
		return note;
	}

	private static Credential getMockCredentialInfo1(){
		Credential credential = new Credential();
		credential.setUrl("https://catlog.com");
		credential.setUsername("0000");
		credential.setPassword("1234");
		return credential;
	}

	private static Credential getMockCredentialInfo2(){
		Credential credential = new Credential();
		credential.setUrl("https://doglog.com");
		credential.setUsername("9999");
		credential.setPassword("4321");
		return credential;
	}

	public String getEncryptedPassword(String plainPassword, Integer userId){

		String key = credentialService.findKeyById(userId);
		return encryptionService.encryptValue(plainPassword,key);
	}
}
