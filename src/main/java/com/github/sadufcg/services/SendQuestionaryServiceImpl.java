package com.github.sadufcg.services;

import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.sadufcg.pojo.Course;
import com.github.sadufcg.pojo.CourseStudent;
import com.github.sadufcg.pojo.Token;
import com.github.sadufcg.repositories.CourseRepository;
import com.github.sadufcg.repositories.CourseStudentRepository;
import com.github.sadufcg.repositories.TokenRepository;
import com.google.common.net.UrlEscapers;

/**
 * Created by antunessilva on 20/03/17.
 */

@Service
public class SendQuestionaryServiceImpl implements SendQuestionaryService {

	@Autowired
	private MailServerService mailService;

	@Autowired
	private CourseRepository courseService;

	@Autowired
	private CourseStudentRepository courseStudentRepository;

	@Autowired
	TokenRepository tokenRepository;

	public void sendQuestionnaire(Course c) {
		for (CourseStudent courseStudent : courseStudentRepository.findByCourse(c)) {
			sendQuestionnaireStudent(courseStudent);
		}
	}

	public void sendQuestionnaireForAllCourses() {
		List<Course> courses = courseService.findAll();
		for (Course c : courses) {
			sendQuestionnaire(c);
		}
	}

	private String generateMailBody(Token token, CourseStudent courseStudent) {
		StringBuilder sb = new StringBuilder();
		String nl = System.lineSeparator();
		sb.append("Olá! A cada semestre realizamos o processo de avaliação docente." + nl + nl);
		sb.append("Sua participação é importante. Cada professor receberá um resumo"
				+ " de como os alunos votaram na sua turma bem como comentários que"
				+ " forem registrados por você. IMPORTANTE, VOCÊ NÃO SERÁ IDENTIFICADO" + " NESTE PROCESSO!" + nl + nl);
		sb.append("Os resultados da avaliação docente ajudam o departamento a definir alterações nas alocações das"
				+ " disciplinas, a identificar as principais áreas a serem trabalhadas e para poder cobrar por"
				+ " melhorias perante a universidade." + nl + nl);
		String disciplina = courseStudent.getCourse().toString();
		sb.append("Pedimos que você avalie a disciplina: " + disciplina + nl + nl);
		sb.append("Para isto, basta acessar o link: https://sad.splab.ufcg.edu.br/#!/answerform/1/"
				+ UrlEscapers.urlFragmentEscaper().escape(disciplina) + "/" + token.getId() + nl);
		sb.append("Importante: você só poderá votar uma única vez e o voto não poderá ser alterado após ser registrado.");
		return sb.toString();
	}

	@Override
	public void sendQuestionnaireStudent(CourseStudent courseStudent) {
		Token token = new Token(courseStudent.getCourse());
		String studentsEmail = courseStudent.getStudent().getEmail();
		String mailBody = generateMailBody(token, courseStudent);
		try {
			mailService.sendEmail(studentsEmail, mailBody);
			courseStudent.setSent(true);
			tokenRepository.save(token);
			courseStudentRepository.save(courseStudent);
		} catch (MessagingException e) {
			System.err.println(">>> MAIL ERROR: " + courseStudent.getId() + " " + studentsEmail
					+ courseStudent.getCourse().getName());
		}
	}

}
