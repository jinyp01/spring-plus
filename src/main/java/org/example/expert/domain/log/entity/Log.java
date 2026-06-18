package org.example.expert.domain.log.entity;

import org.example.expert.domain.common.entity.Timestamped;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long requesterId;

	@Column(nullable = false)
	private Long todoId;

	@Column(nullable = false)
	private Long managerUserId;

	@Column(nullable = false)
	private String requesterEmail;

	public Log(Long requesterId, Long todoId, Long managerUserId, String requesterEmail) {
		this.requesterId = requesterId;
		this.todoId = todoId;
		this.managerUserId = managerUserId;
		this.requesterEmail = requesterEmail;
	}
}
