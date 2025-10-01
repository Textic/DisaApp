package com.textic.disaapp.viewmodel

import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AdditionalUserInfo
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockAuthResult: AuthResult

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockCollectionReference: CollectionReference

    @Mock
    private lateinit var mockDocumentReference: DocumentReference

    @Mock
    private lateinit var mockAdditionalUserInfo: AdditionalUserInfo

    @Mock
    private lateinit var mockFirebaseAppInstance: FirebaseApp

    private lateinit var authViewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockedFirebaseAuth: MockedStatic<FirebaseAuth>
    private lateinit var mockedFirebaseFirestore: MockedStatic<FirebaseFirestore>
    private lateinit var mockedFirebaseApp: MockedStatic<FirebaseApp>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Mock FirebaseApp first
        mockedFirebaseApp = Mockito.mockStatic(FirebaseApp::class.java)
        mockedFirebaseApp.`when`<FirebaseApp>(FirebaseApp::getInstance).thenReturn(mockFirebaseAppInstance)

        // Mock FirebaseAuth
        mockedFirebaseAuth = Mockito.mockStatic(FirebaseAuth::class.java)
        mockedFirebaseAuth.`when`<FirebaseAuth>(FirebaseAuth::getInstance).thenReturn(mockAuth)

        // Mock FirebaseFirestore
        mockedFirebaseFirestore = Mockito.mockStatic(FirebaseFirestore::class.java)
        mockedFirebaseFirestore.`when`<FirebaseFirestore>(FirebaseFirestore::getInstance).thenReturn(mockFirestore)
        
        authViewModel = AuthViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedFirebaseFirestore.close()
        mockedFirebaseAuth.close()
        mockedFirebaseApp.close()
    }

    //region Register Tests
    @Test
    fun `register success`() = runTest {
        val name = "Test User"
        val email = "test@example.com"
        val pass = "password123"
        val uid = "testUid"

        whenever(mockAuth.createUserWithEmailAndPassword(email, pass))
            .thenReturn(Tasks.forResult(mockAuthResult))
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(uid)
        whenever(mockFirestore.collection("users")).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document(uid)).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.set(any<User>())).thenReturn(Tasks.forResult(null))

        val result = authViewModel.register(name, email, pass)

        assertEquals(RegistrationResult.Success, result)
        verify(mockAuth).createUserWithEmailAndPassword(email, pass)
        verify(mockFirestore.collection("users").document(uid)).set(User(name, email))
    }

    @Test
    fun `register email already exists`() = runTest {
        val name = "Test User"
        val email = "test@example.com"
        val pass = "password123"

        whenever(mockAuth.createUserWithEmailAndPassword(email, pass))
            .thenReturn(Tasks.forException(FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "Email already in use")))

        val result = authViewModel.register(name, email, pass)

        assertEquals(RegistrationResult.EmailAlreadyExists, result)
        verify(mockAuth).createUserWithEmailAndPassword(email, pass)
    }

    @Test
    fun `register general error`() = runTest {
        val name = "Test User"
        val email = "test@example.com"
        val pass = "password123"
        val errorMessage = "A general error occurred"

        whenever(mockAuth.createUserWithEmailAndPassword(email, pass))
            .thenReturn(Tasks.forException(RuntimeException(errorMessage)))

        val result = authViewModel.register(name, email, pass)

        assertTrue(result is RegistrationResult.Error)
        assertEquals(errorMessage, (result as RegistrationResult.Error).message)
        verify(mockAuth).createUserWithEmailAndPassword(email, pass)
    }
    //endregion

    //region Login Tests
    @Test
    fun `login success`() = runTest {
        val email = "test@example.com"
        val password = "password123"

        whenever(mockAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(Tasks.forResult(mockAuthResult))

        val result = authViewModel.login(email, password)

        assertEquals(LoginResult.Success, result)
        verify(mockAuth).signInWithEmailAndPassword(email, password)
    }

    @Test
    fun `login wrong credentials`() = runTest {
        val email = "test@example.com"
        val password = "wrongpassword"

        whenever(mockAuth.signInWithEmailAndPassword(email, password))
            .thenReturn(Tasks.forException(FirebaseAuthInvalidCredentialsException("ERROR_INVALID_CREDENTIAL", "Invalid credentials")))

        val result = authViewModel.login(email, password)

        assertEquals(LoginResult.WrongCredentials, result)
        verify(mockAuth).signInWithEmailAndPassword(email, password)
    }
    //endregion

    //region SignInWithGoogle Tests
    @Test
    fun `signInWithGoogle success new user`() = runTest {
        val idToken = "testIdToken"
        val displayName = "Google User"
        val email = "googleuser@example.com"
        val uid = "googleUid"

        whenever(mockAuth.signInWithCredential(any())) // Simpler: if any AuthCredential works
            .thenReturn(Tasks.forResult(mockAuthResult))
        whenever(mockAuthResult.additionalUserInfo).thenReturn(mockAdditionalUserInfo)
        whenever(mockAdditionalUserInfo.isNewUser).thenReturn(true)
        whenever(mockAuthResult.user).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.displayName).thenReturn(displayName)
        whenever(mockFirebaseUser.email).thenReturn(email)
        whenever(mockFirebaseUser.uid).thenReturn(uid)
        whenever(mockFirestore.collection("users")).thenReturn(mockCollectionReference)
        whenever(mockCollectionReference.document(uid)).thenReturn(mockDocumentReference)
        whenever(mockDocumentReference.set(any<User>())).thenReturn(Tasks.forResult(null))

        val result = authViewModel.signInWithGoogle(idToken)

        assertEquals(LoginResult.Success, result)
        verify(mockAuth).signInWithCredential(any())
        verify(mockFirestore.collection("users").document(uid)).set(User(displayName, email))
    }

    @Test
    fun `signInWithGoogle success existing user`() = runTest {
        val idToken = "testIdToken"

        whenever(mockAuth.signInWithCredential(any()))
            .thenReturn(Tasks.forResult(mockAuthResult))
        whenever(mockAuthResult.additionalUserInfo).thenReturn(mockAdditionalUserInfo)
        whenever(mockAdditionalUserInfo.isNewUser).thenReturn(false)
        Mockito.lenient().`when`(mockAuthResult.user).thenReturn(mockFirebaseUser)

        val result = authViewModel.signInWithGoogle(idToken)

        assertEquals(LoginResult.Success, result)
        verify(mockAuth).signInWithCredential(any())
        verify(mockCollectionReference, never()).document(any())
    }
    //endregion
}