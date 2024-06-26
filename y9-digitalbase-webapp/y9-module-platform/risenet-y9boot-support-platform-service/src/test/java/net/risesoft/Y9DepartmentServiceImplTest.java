package net.risesoft;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.risesoft.manager.org.CompositeOrgBaseManager;
import net.risesoft.manager.org.Y9DepartmentManager;
import net.risesoft.repository.Y9DepartmentPropRepository;
import net.risesoft.repository.Y9DepartmentRepository;
import net.risesoft.service.org.impl.Y9DepartmentServiceImpl;

public class Y9DepartmentServiceImplTest {

    @Mock
    private Y9DepartmentRepository y9DepartmentRepository;
    @Mock
    private Y9DepartmentPropRepository y9DepartmentPropRepository;

    @Mock
    private CompositeOrgBaseManager compositeOrgBaseManager;
    @Mock
    private Y9DepartmentManager y9DepartmentManager;

    @InjectMocks
    private Y9DepartmentServiceImpl y9DepartmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*@Test
    public void testChangeDisable() {
        when(y9DepartmentManager.getByIdNotCache("11")).thenReturn(new Y9Department());
        when(y9DepartmentManager.save(any())).thenReturn(new Y9Department());
        y9DepartmentService.changeDisable("11");
    
        when(y9DepartmentManager.getByIdNotCache("22")).thenThrow(
            Y9ExceptionUtil.notFoundException(OrgUnitErrorCodeEnum.DEPARTMENT_NOT_FOUND, "22"));
        
        Assertions.assertThrows(Y9NotFoundException.class, () -> y9DepartmentService.changeDisable("22"));
        
    }*/
}
