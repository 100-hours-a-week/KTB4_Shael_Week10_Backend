package org.example.communityservice.common.util;

import lombok.NoArgsConstructor;
import org.example.communityservice.entity.User;

@NoArgsConstructor
public class UserDisplayUtils {
    private static final String WITHDRAWN_NICKNAME = "탈퇴 회원";

    public static String nickname(User user){
        if(user.getDeletedAt() != null){
            return WITHDRAWN_NICKNAME;
        }
        else{
            return user.getNickname();
        }
    }

    public static String profileStoredFilename(User user){
        return "/images/profiles/" + user.getProfileStoredFilename();
    }
}
