package org.sopt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long createMember(String name) {
        Member member = new Member(name);
        Member savedMember = memberRepository.save(member);

        return savedMember.getId();
    }

    @Transactional
    public void withdrawMember(long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(
                        () -> new MemberNotFoundException(memberId)
                );

        member.withdraw();
    }
}