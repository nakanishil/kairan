package com.example.kairan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kairan.entity.District;
import com.example.kairan.form.AdminDistrictRegiForm;
import com.example.kairan.repository.DistrictRepository;

@ExtendWith(MockitoExtension.class)
public class AdminDistrictServiceTest {
	
 	@Mock
    private DistrictRepository districtRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminDistrictService adminDistrictService;

	@Test
	void regiDistrict_新規登録が成功する() {
		
		// arrange
		AdminDistrictRegiForm form = new AdminDistrictRegiForm();
		form.setName("東町");
	    form.setRegioncode("RC001");
	    form.setAssociation("東町内会");
	    form.setArea("1区");
	    form.setDescription("テスト用の町内会");
	    
	    when(districtRepository.findByAssociationAndDeletedAtIsNull("東町内会"))
	    	.thenReturn(List.of());
	    
	    when(districtRepository.save(any(District.class)))
	    	.thenAnswer(invocation -> invocation.getArgument(0));

	    
	    // act
	    District result = adminDistrictService.regiDistrict(form);
	    
	 // Assert
	    assertEquals("東町", result.getName());
	    assertEquals("RC001", result.getRegionCode());
	    assertEquals("東町内会", result.getAssociation());
	    assertEquals("1区", result.getArea());
	    assertEquals("テスト用の町内会", result.getDescription());

	    verify(districtRepository).save(any(District.class));
	}
	
	@Test
	void regiDistrict_重複するassociationがある場合は例外をスローする() {
	    // arrange
	    AdminDistrictRegiForm form = new AdminDistrictRegiForm();
	    form.setName("東町");
	    form.setRegioncode("RC001");
	    form.setAssociation("東町内会");
	    form.setArea("1区");
	    form.setDescription("重複テスト");

	    when(districtRepository.findByAssociationAndDeletedAtIsNull("東町内会"))
	        .thenReturn(List.of(new District()));

	    // act & assert
	    IllegalArgumentException exception = assertThrows(
	        IllegalArgumentException.class,
	        () -> adminDistrictService.regiDistrict(form)
	    );

	    assertEquals("入力した町内会名は既に存在します", exception.getMessage());

	    verify(districtRepository, never()).save(any(District.class));
	}

}
