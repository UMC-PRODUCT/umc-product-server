package com.umc.product.techblog.application.port.in.command;

import com.umc.product.techblog.application.port.in.command.dto.DeleteTechBlogCommentCommand;

public interface DeleteTechBlogCommentUseCase {

    void delete(DeleteTechBlogCommentCommand command);

    void deleteByAdmin(Long commentId, Long adminMemberId);
}
