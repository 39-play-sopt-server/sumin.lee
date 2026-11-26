package org.sopt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshSessionRepository sessions;

    public MemberService(MemberRepository memberRepository, RefreshSessionRepository sessions) {
        this.memberRepository = memberRepository;
        this.sessions = sessions;
    }

    @Transactional
    public void withdrawMember(long memberId) {
        Member member = memberRepository.findActiveForUpdate(memberId)
                .orElseThrow(
                        () -> new MemberNotFoundException(memberId)
                );

        member.withdraw();
        sessions.deleteForMember(memberId);
    }
}
