package com.umc.product.storage.application.port.in.query;

import com.umc.product.storage.application.port.in.query.dto.FileInfo;

/**
 * 파일 조회 UseCase
 */
public interface GetFileUseCase {
    /**
     * 내부용 인터페이스 입니다. 절대 API 단에 직접 조회할 수 있도록 하지 마세요.
     * <p>
     * fileId를 받아서, 파일의 정보를 반환하는 method 입니다.
     * <p>
     * 해당 과정을 통해서 사용자에게 응답 시에 fileId를 제공하는 것이 아닌 파일에 직접 접근할 수 있는 URL을 제공합니다.
     *
     * @param fileId 파일 ID
     * @return 파일 정보
     */
    FileInfo getById(String fileId);

    /**
     * 파일이 존재하는지 확인합니다. Helper method 입니다.
     *
     * @param fileId 파일 ID
     * @return 존재 여부
     */
    boolean existsById(String fileId);
}
